package org.honton.chas.buildx.maven.plugin.cmdline;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

  public BuildxBuild addBuildArgs(Map<String, String> buildArguments) {
    if (buildArguments != null) {
      buildArguments.forEach(
          (k, v) -> {
            addParameter("--build-arg", k + "=" + v);
          });
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
      Set<String> set = new LinkedHashSet<>();
      platforms.stream()
          .flatMap(p -> p != null ? Arrays.stream(p.split(",")) : Stream.of())
          .map(String::strip)
          .filter(p -> !p.isEmpty())
          .forEach(set::add);

      if (!set.isEmpty()) {
        addParameter("--platform", String.join(",", set));
        return set.size() > 1;
      }
    }
    return false;
  }

  public BuildxBuild addPlatformsAndImage(List<String> platforms, String image) {
    String param = addPlatforms(platforms) && isPodman ? "--manifest" : "--tag";
    addParameter(param, image);
    return this;
  }

  public BuildxBuild addPlatformAndImage(String platform, String image) {
    addParameter("--platform", platform);
    addParameter("--tag", image);
    return this;
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
