package org.honton.chas.buildx.maven.plugin.config;

import java.util.List;
import lombok.ToString;
import org.apache.maven.plugins.annotations.Parameter;

/** Layer of files in the image */
@ToString
public class LayerConfig {
  /** Owner:Group of the files in the image */
  @Parameter public String chown;

  /** Permissions of the files in the image */
  @Parameter public String chmod;

  /** Name of context containing srcs */
  @Parameter public String context;

  /** Files relative to the context to be copied. (golang wildcards supported) */
  @Parameter(required = true)
  public List<String> srcs;

  /** Absolute destination in the image where files are copied */
  @Parameter(required = true)
  public String dest;
}
