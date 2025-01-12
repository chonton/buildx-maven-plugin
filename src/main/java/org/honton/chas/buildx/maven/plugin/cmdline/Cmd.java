package org.honton.chas.buildx.maven.plugin.cmdline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import org.honton.chas.buildx.maven.plugin.config.ConnectionCfg;

public class Cmd<T extends Cmd<?>> {
  @Getter protected final List<String> command;
  protected final boolean isPodman;

  public Cmd(String cli) {
    command = new ArrayList<>();
    isPodman = cli.endsWith("podman");
    command.add(cli);
  }

  public Cmd(ConnectionCfg goal) {
    this(goal.getCli());
  }

  public T addCmd(String cmd) {
    command.add(cmd);
    return (T) this;
  }

  public T addParameter(String... parameter) {
    command.addAll(Arrays.asList(parameter));
    return (T) this;
  }
}
