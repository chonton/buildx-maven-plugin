package org.honton.chas.buildx.maven.plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import lombok.Getter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.honton.chas.buildx.maven.plugin.cmdline.Cmd;
import org.honton.chas.buildx.maven.plugin.cmdline.ExecHelper;
import org.honton.chas.buildx.maven.plugin.config.ConnectionCfg;

/** image build goal base functionality */
public abstract class ImageGoal extends AbstractMojo implements ConnectionCfg {

  /** image build command line interface */
  @Parameter(property = "buildx.cli", defaultValue = "docker")
  @Getter
  public String cli;

  /** Skip upgrade */
  @Parameter(property = "buildx.skip", defaultValue = "false")
  boolean skip;

  // work variables ...
  private Path pwd; // current working directory

  public final void execute() throws MojoFailureException, MojoExecutionException {
    if (skip) {
      getLog().info("skipping image build");
    } else {
      try {
        pwd = Path.of("").toAbsolutePath();
        doExecute();
      } catch (IOException | ExecutionException | InterruptedException e) {
        throw new MojoFailureException(e.getMessage(), e);
      }
    }
  }

  protected abstract void doExecute()
      throws MojoExecutionException, IOException, ExecutionException, InterruptedException;

  public int executeCommand(Cmd<?> generator, boolean throwOnError) throws MojoExecutionException {
    return executeCommand(generator, null, throwOnError);
  }

  protected int executeCommand(Cmd<?> generator, String stdin, boolean throwOnError)
      throws MojoExecutionException {
    return new ExecHelper(this).createAndWait(generator.getCommand(), stdin, throwOnError);
  }

  protected String shortestPath(Path dst) {
    if (!dst.isAbsolute()) {
      dst = dst.toAbsolutePath();
    }
    String relative = pwd.relativize(dst).toString();
    String absolute = dst.toString();
    if (absolute.length() < relative.length()) {
      return absolute;
    }
    return relative.isEmpty() ? "./" : relative;
  }
}
