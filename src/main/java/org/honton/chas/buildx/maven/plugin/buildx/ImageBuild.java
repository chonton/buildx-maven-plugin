package org.honton.chas.buildx.maven.plugin.buildx;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.honton.chas.buildx.maven.plugin.Containerfile;
import org.honton.chas.buildx.maven.plugin.cmdline.Buildx;
import org.honton.chas.buildx.maven.plugin.cmdline.BuildxBuild;

/** Create a container image from the Containerfile directions and files from context */
@Mojo(name = "build", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class ImageBuild extends Containerfile {

  public static final String BUILDX_MAVEN = "buildx-maven";

  /** Fully qualified image name containing registry prefix, repository name, and version */
  @Parameter(property = "buildx.image", required = true)
  protected String image;

  /** The os/arch of the built image */
  @Parameter(property = "buildx.platforms")
  protected List<String> platforms;

  /** Map of build arguments */
  @Parameter public Map<String, String> buildArguments;

  /** The name of an existing builder to use */
  @Parameter(property = "buildx.builder", defaultValue = BUILDX_MAVEN)
  protected String builder;

  /** Map of context name to location */
  @Parameter public Map<String, String> contexts;

  /** Load resulting image into local image cache */
  @Parameter(property = "buildx.load", defaultValue = "true")
  boolean load;

  /** Include provenance attestation. Acceptable values are "min", "max", "false" */
  @Parameter(property = "buildx.provenance")
  public String provenance;

  /** Include software bill of materials attestation. */
  @Parameter(property = "buildx.sbom", defaultValue = "false")
  public boolean sbom;

  @Parameter(defaultValue = "${session}", required = true, readonly = true)
  private MavenSession session;

  @SneakyThrows
  static String imageNameHash(String image) {
    String canonical = canonicalImage(image);

    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    return Base64.getUrlEncoder()
        .encodeToString(digest.digest(canonical.getBytes(StandardCharsets.UTF_8)))
        .substring(0, 43);
  }

  static String canonicalImage(String image) {
    // [HOST[:PORT_NUMBER]/][NAMESPACE/]REPOSITORY[:TAG]
    String[] parts = image.split("/");

    StringBuilder sb = new StringBuilder();
    if (parts.length == 1) {
      sb.append("docker.io");
    } else {
      String registry = parts[0];
      int endIdx = registry.length();
      int colonIdx = registry.indexOf(':');
      if (colonIdx >= 0) {
        String port = registry.substring(colonIdx + 1);
        if (port.equals("443")) {
          endIdx = colonIdx;
        }
      }
      sb.append(registry, 0, endIdx);
    }
    sb.append("/");

    if (parts.length <= 2) {
      sb.append("library/");
    } else {
      for (int i = 1; i < parts.length - 1; i++) {
        sb.append(parts[i]).append("/");
      }
    }

    String repository = parts[parts.length - 1];
    sb.append(repository);
    if (repository.indexOf(':') < 0) {
      sb.append(":latest");
    }
    return sb.toString();
  }

  static String nativePlatform() {
    return operatingSystem() + "/" + architecture();
  }

  static String operatingSystem() {
    String osName = System.getProperty("os.name").toLowerCase();
    return osName.contains("win") ? "windows" : "linux";
  }

  static String architecture() {
    String osArch = System.getProperty("os.arch");
    switch (osArch) {
      case "aarch64":
        return "arm64";
      case "x86_64":
        return "amd64";
      default:
        return osArch;
    }
  }

  protected String ctxDir() {
    return shortestPath(Path.of(context));
  }

  protected Map<String, String> contexts() {
    if (contexts == null) {
      return Map.of();
    }
    return contexts.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, e -> shortestPath(Path.of(e.getValue()))));
  }

  @Override
  protected void doExecute() throws MojoExecutionException {
    if (BUILDX_MAVEN.equals(builder)) {
      Buildx<?> createCmd =
          new Buildx<>(this)
              .addCmd("create")
              .addParameter("--driver", "docker-container")
              .addParameter("--name", builder);
      executeCommand(createCmd, false);
    }

    String ctxDir = ctxDir();
    Map<String, String> additional = contexts();
    BuildxBuild buildCmd =
        new BuildxBuild(this, builder)
            .addPlatformsAndImage(platforms, image)
            .addContainerfileAndCtx(containerFile, ctxDir, additional);
    executeCommand(buildCmd, true);

    if (load) {
      loadImage(builder, ctxDir, additional);
    }
  }

  /*
  private void hookCleanup(String builderName) {
    MavenExecutionRequest request = session.getRequest();
    ExecutionListener originalListener = request.getExecutionListener();
    request.setExecutionListener(
        (ExecutionListener)
            Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] {ExecutionListener.class},
                (proxy, method, args) -> {
                  if (method.getName().equals("sessionEnded")) {
                    cleanup(builderName);
                  }
                  if (originalListener != null) {
                    return method.invoke(originalListener, args);
                  }
                  switch (method.getName()) {
                    case "equals":
                      return false;
                    case "hashCode":
                      return 0;
                    default:
                      return null;
                  }
                }));
  }

  private void cleanup(String builderName) throws MojoExecutionException {
    executeCommand(new Buildx<>(this).addCmd("rm").addParameter(builderName), true);
  }
  */

  private void loadImage(String builderName, String ctxDir, Map<String, String> additional)
      throws MojoExecutionException {
    BuildxBuild buildCmd =
        new BuildxBuild(this, builderName)
            .addPlatformAndImage(nativePlatform(), image)
            .addParameter("--output", "type=docker")
            .addContainerfileAndCtx(containerFile, ctxDir, additional);

    executeCommand(buildCmd, true);
  }
}
