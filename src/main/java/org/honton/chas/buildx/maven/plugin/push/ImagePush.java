package org.honton.chas.buildx.maven.plugin.push;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.honton.chas.buildx.maven.plugin.buildx.ImageBuild;
import org.honton.chas.buildx.maven.plugin.cmdline.BuildxBuild;
import org.honton.chas.buildx.maven.plugin.cmdline.Cmd;

/** Push image to registry */
@Mojo(name = "push", defaultPhase = LifecyclePhase.DEPLOY, threadSafe = true)
public class ImagePush extends ImageBuild {

  /** Skip push */
  @Parameter(property = "buildx.skipPush", defaultValue = "false")
  boolean skipPush;

  @Override
  protected final void doExecute() throws MojoExecutionException {
    if (skipPush) {
      getLog().info("skipping image push");
    } else {
      BuildxBuild buildCmd = new BuildxBuild(this, builder);
      if (buildCmd.isPodman()) {
        podmanPush();
      } else {
        dockerPush(buildCmd);
      }
    }
  }

  private void podmanPush() throws MojoExecutionException {
    Cmd.iterateImageTags(
        registries,
        image,
        fqin -> {
          Cmd<?> pushCmd = new Cmd<>(this).addCmd("manifest").addCmd("push").addParameter(fqin);
          executeCommand(pushCmd, true);
        });
  }

  private void dockerPush(BuildxBuild buildCmd) throws MojoExecutionException {
    buildCmd.addParameter("--push");
    buildCmd.addParameters("--platform", Cmd.allPlatforms(platforms));
    buildCmd.addImages(registries, image, "--tag");
    buildCmd.addParameters("--output", "type=registry");
    buildCmd.addContainerfileAndCtx(containerFile, ctxDir(), contexts());
    executeCommand(buildCmd, true);
  }
}
