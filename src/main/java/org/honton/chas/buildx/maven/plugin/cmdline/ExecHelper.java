package org.honton.chas.buildx.maven.plugin.cmdline;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.honton.chas.buildx.maven.plugin.ImageGoal;

public class ExecHelper {

  private final Consumer<String> infoLine;

  public ExecHelper(ImageGoal goal) {
    Log log = goal.getLog();
    infoLine =
        (lineText) -> {
          if (lineText != null) {
            log.info(lineText);
          }
        };
  }

  public int createAndWait(List<String> command, String stdin, boolean throwOnError)
      throws MojoExecutionException {
    try {
      infoLine.accept(String.join(" ", command));

      Process process =
          new ProcessBuilder(command)
              .redirectOutput(Redirect.INHERIT)
              .redirectError(Redirect.INHERIT)
              .start();

      OutputStream os = process.getOutputStream();
      if (stdin != null) {
        os.write(stdin.getBytes(StandardCharsets.UTF_8));
      }
      os.close();

      int exitCode = process.waitFor();
      if (exitCode != 0 && throwOnError) {
        throw new MojoExecutionException("command exited with error - " + exitCode);
      }
      return exitCode;
    } catch (IOException | InterruptedException ex) {
      throw new MojoExecutionException(ex);
    }
  }
}
