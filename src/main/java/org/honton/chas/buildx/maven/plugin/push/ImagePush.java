package org.honton.chas.buildx.maven.plugin.push;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.honton.chas.buildx.maven.plugin.buildx.ImageBuild;
import org.honton.chas.buildx.maven.plugin.cmdline.BuildxBuild;

/** Push image to registry */
@Mojo(name = "push", defaultPhase = LifecyclePhase.DEPLOY, threadSafe = true)
public class ImagePush extends ImageBuild {

  @Override
  protected final void doExecute() throws MojoExecutionException {
    BuildxBuild buildCmd =
        new BuildxBuild(this, builder)
            .addPlatformsAndImage(platforms, image)
            .addParameter("--output", "type=registry")
            .addContainerfileAndCtx(containerFile, ctxDir(), contexts());
    executeCommand(buildCmd, true);
  }
}
