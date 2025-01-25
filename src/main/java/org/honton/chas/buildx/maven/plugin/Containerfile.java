package org.honton.chas.buildx.maven.plugin;

import org.apache.maven.plugins.annotations.Parameter;

public abstract class Containerfile extends ImageGoal {

  /** Build instruction file, relative to context */
  @Parameter(property = "buildx.containerFile")
  public String containerFile;

  /** Directory containing source content for build */
  @Parameter(
      property = "buildx.context",
      required = true,
      defaultValue = "${project.build.directory}/context")
  public String context;

  protected String containerFile() {
    return containerFile != null ? containerFile : defaultContainerFile();
  }

  protected String defaultContainerFile() {
    return cli.endsWith("podman") ? "Containerfile" : "Dockerfile";
  }
}
