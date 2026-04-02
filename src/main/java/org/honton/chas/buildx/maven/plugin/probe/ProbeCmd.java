package org.honton.chas.buildx.maven.plugin.probe;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/** Set buildx.cli property to 'podman' if docker is alias or link to podman */
@Mojo(name = "probe", defaultPhase = LifecyclePhase.INITIALIZE, threadSafe = true)
public class ProbeCmd extends AbstractMojo {

  @Parameter(defaultValue = "${session}", required = true, readonly = true)
  private MavenSession session;

  @Override
  public void execute() {
    try {
      Process process =
          new ProcessBuilder("docker", "info").redirectError(Redirect.INHERIT).start();
      process.getOutputStream().close();
      byte[] result = process.getInputStream().readAllBytes();
      int exitCode = process.waitFor();
      if (exitCode == 0 && !new String(result, StandardCharsets.US_ASCII).contains("podman")) {
        return;
      }
    } catch (IOException | InterruptedException ex) {
      getLog().debug("ignored exception: " + ex.getMessage());
    }
    getLog().info("setting buildx.cli = podman");
    session.getUserProperties().setProperty("buildx.cli", "podman");
  }
}
