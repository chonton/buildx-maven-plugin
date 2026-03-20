package org.honton.chas.buildx.maven.plugin.cmdline;

import java.util.List;
import java.util.Map;
import org.apache.maven.plugin.MojoExecutionException;
import org.honton.chas.buildx.maven.plugin.buildx.ImageBuild;

public class BuildxBuild extends Buildx<BuildxBuild> {

  public BuildxBuild(ImageBuild goal, String builderName) {
    super(goal);
    addCmd("build");
    if (!isPodman) {
      addParameters("--builder", builderName);
    }
    if (goal.provenance != null && !"false".equals(goal.provenance)) {
      addParameters("--provenance", goal.provenance);
    }
    if (goal.sbom) {
      addParameters("--sbom", "true");
    }
    addBuildArgs(goal.buildArguments);
  }

  public BuildxBuild addBuildArgs(Map<String, String> buildArguments) {
    if (buildArguments != null) {
      buildArguments.forEach((k, v) -> addParameters("--build-arg", k + "=" + v));
    }
    return this;
  }

  public BuildxBuild addImages(List<String> registries, String image, String argName)
      throws MojoExecutionException {
    iterateImageTags(registries, image, fqin -> addParameters(argName, fqin));
    return this;
  }

  public BuildxBuild addContainerfileAndCtx(
      String file, String context, Map<String, String> additional) {
    additional.forEach((k, v) -> addParameters("--build-context", k + "=" + v));
    if (file != null) {
      addParameters("--file", file);
    }
    addParameter(context);
    return this;
  }
}
