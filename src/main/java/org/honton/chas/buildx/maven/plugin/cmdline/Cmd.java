package org.honton.chas.buildx.maven.plugin.cmdline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import lombok.Getter;
import org.apache.maven.plugin.MojoExecutionException;
import org.honton.chas.buildx.maven.plugin.config.ConnectionCfg;

public class Cmd<T extends Cmd<?>> {
  @Getter protected final List<String> command;
  @Getter protected final boolean isPodman;

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

  public T addParameters(String p1, String p2) {
    command.add(p1);
    command.add(p2);
    return (T) this;
  }

  public T addParameter(String parameter) {
    command.add(parameter);
    return (T) this;
  }

  public static List<String> splitList(List<String> stringList) {
    return stringList.stream()
        .flatMap(p -> p != null ? Arrays.stream(p.split(",")) : Stream.of())
        .map(String::strip)
        .filter(p -> !p.isEmpty())
        .toList();
  }

  private static String removeTrailingSlash(String registry) {
    int idxOfLastChar = registry.length() - 1;
    return registry.charAt(idxOfLastChar) == '/' ? registry.substring(0, idxOfLastChar) : registry;
  }

  private static String removeLeadingSlash(String image) {
    return image.charAt(0) == '/' ? image.substring(1) : image;
  }

  private static String fqin(String registry, String image) {
    return removeTrailingSlash(registry) + '/' + removeLeadingSlash(image);
  }

  @FunctionalInterface
  public interface ImageConsumer {
    void accept(String fqin) throws MojoExecutionException;
  }

  /**
   * @param platforms the os/arch of specified platforms or native platform
   */
  public static String allPlatforms(List<String> platforms) {
    if (platforms != null) {
      List<String> allPlatforms = splitList(platforms);
      if (!allPlatforms.isEmpty()) {
        return String.join(",", allPlatforms);
      }
    }
    return nativePlatform();
  }

  public static String nativePlatform() {
    return operatingSystem() + "/" + architecture();
  }

  static String operatingSystem() {
    String osName = System.getProperty("os.name").toLowerCase();
    return osName.contains("win") ? "windows" : "linux";
  }

  static String architecture() {
    String osArch = System.getProperty("os.arch");
    return switch (osArch) {
      case "aarch64" -> "arm64";
      case "x86_64" -> "amd64";
      default -> osArch;
    };
  }

  public static void iterateImageTags(List<String> registries, String image, ImageConsumer consumer)
      throws MojoExecutionException {
    if (registries != null) {
      List<String> strings = splitList(registries);
      if (!strings.isEmpty()) {
        for (String registry : strings) {
          consumer.accept(fqin(registry, image));
        }
        return;
      }
    }
    consumer.accept(image);
  }
}
