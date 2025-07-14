package org.honton.chas.buildx.maven.plugin.cmdline;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.honton.chas.buildx.maven.plugin.buildx.ImageBuild;

public class BuildxBuild extends Buildx<BuildxBuild> {

  public BuildxBuild(ImageBuild goal, String builderName) {
    super(goal);
    addCmd("build");
    addParameter("--builder", builderName);
    if (goal.provenance != null) {
      addParameter("--provenance", goal.provenance);
    }
    if (goal.sbom) {
      addParameter("--sbom", "true");
    }
    addBuildArgs(goal.buildArguments);
  }

  private static Stream<String> splitList(List<String> stringList) {
    return stringList.stream()
        .flatMap(p -> p != null ? Arrays.stream(p.split(",")) : Stream.of())
        .map(String::strip)
        .filter(p -> !p.isEmpty());
  }

  private static String removeTrailingSlash(String registry) {
    int idxOfLastChar = registry.length() - 1;
    return registry.charAt(idxOfLastChar) == '/' ? registry.substring(0, idxOfLastChar) : registry;
  }

  private static String removeLeadingSlash(String image) {
    return image.charAt(0) == '/' ? image.substring(1) : image;
  }

  private static String fqin(String image, String registry) {
    return removeTrailingSlash(registry) + '/' + removeLeadingSlash(image);
  }

  public BuildxBuild addBuildArgs(Map<String, String> buildArguments) {
    if (buildArguments != null) {
      buildArguments.forEach((k, v) -> addParameter("--build-arg", k + "=" + v));
    }
    return this;
  }

  /**
   * Add the platform option
   *
   * @param platforms the os/arch of the resulting image(s)
   * @return true if multi-platform
   */
  private boolean addPlatforms(List<String> platforms) {
    if (platforms != null) {
      String platformParam = splitList(platforms).collect(Collectors.joining(","));
      if (!platformParam.isEmpty()) {
        addParameter("--platform", platformParam);
        return true;
      }
    }
    return false;
  }

  public BuildxBuild addPlatformsAndImage(
      List<String> platforms, List<String> registries, String image) {
    String param = addPlatforms(platforms) && isPodman ? "--manifest" : "--tag";
    addImages(param, registries, image);
    return this;
  }

  public BuildxBuild addPlatformAndImage(String platform, List<String> registries, String image) {
    addParameter("--platform", platform);
    addImages("--tag", registries, image);
    return this;
  }

  private void addImages(String param, List<String> registries, String image) {
    AtomicBoolean needTag = new AtomicBoolean(true);
    if (registries != null) {
      splitList(registries)
          .forEach(
              registry -> {
                addParameter(param, fqin(image, registry));
                needTag.set(false);
              });
    }
    if (needTag.get()) {
      addParameter(param, image);
    }
  }

  public BuildxBuild addContainerfileAndCtx(
      String file, String context, Map<String, String> additional) {
    additional.forEach((k, v) -> addParameter("--build-context", k + "=" + v));
    if (file != null) {
      addParameter("--file", file);
    }
    addParameter(context);
    return this;
  }
}
