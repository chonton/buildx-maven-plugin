package org.honton.chas.buildx.maven.plugin.cmdline;

import org.honton.chas.buildx.maven.plugin.buildx.ImageBuild;

public class Buildx<T extends Buildx<?>> extends Cmd<T> {

  public Buildx(ImageBuild goal) {
    super(goal);
    addCmd("buildx");
  }
}
