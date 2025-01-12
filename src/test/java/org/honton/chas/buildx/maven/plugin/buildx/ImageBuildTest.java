package org.honton.chas.buildx.maven.plugin.buildx;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ImageBuildTest {

  @Test
  void defaultRegistry() {
    Assertions.assertEquals("docker.io/library/ubuntu:latest", ImageBuild.canonicalImage("ubuntu"));
  }

  @Test
  void defaultNamespace() {
    Assertions.assertEquals(
        "docker.io/library/ubuntu:3.13", ImageBuild.canonicalImage("docker.io/ubuntu:3.13"));
  }

  @Test
  void defaultTag() {
    Assertions.assertEquals(
        "docker.io/library/ubuntu:latest", ImageBuild.canonicalImage("docker.io/library/ubuntu"));
  }

  @Test
  void multipleNamespaceSegments() {
    Assertions.assertEquals(
        "example.com/a/b/ubuntu:1.2.3", ImageBuild.canonicalImage("example.com/a/b/ubuntu:1.2.3"));
  }

  @Test
  void securePort() {
    Assertions.assertEquals(
        "example.com/ns/ubuntu:v15", ImageBuild.canonicalImage("example.com:443/ns/ubuntu:v15"));
  }

  @Test
  void nonstandardPort() {
    Assertions.assertEquals(
        "example.com:8080/ns/ubuntu:28",
        ImageBuild.canonicalImage("example.com:8080/ns/ubuntu:28"));
  }

  @Test
  void ok() {
    Assertions.assertEquals(
        "k45xC4Ewt20SXloY4wPOVtaLhcXQk0g8eGhHpxBGwuM",
        ImageBuild.imageNameHash("private.example.com/namespace/repository"));
  }
}
