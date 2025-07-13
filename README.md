# buildx-maven-plugin

Build and push images using docker or podman buildx. This plugin has four goals:

1. [Login to registry](#login-goal)
2. [Create Containerfile from base image and copy directives](#containerfile-goal)
3. [Build image from Containerfile and context](#build-goal)
4. [Push image to registry](#push-goal)

# Rationale

Build images using [docker buildx](https://docs.docker.com/reference/cli/docker/buildx/) or
[podman build](https://docs.podman.io/en/stable/markdown/podman-build.1.html) with this
plugin's [containerfile](#containerfile-goal) and [build](#build-goal) goals.

# Plugin Reports

Plugin reports available at
[plugin info](https://chonton.github.io/buildx-maven-plugin/plugin-info.html).

## Login Goal

The [login](https://chonton.github.io/buildx-maven-plugin/login-mojo.html) goal binds by default to
the **prepare-package** phase. This goal executes `docker login` or `podman login` with credentials
from **settings.xml** or specified in the configuration.

### Login Configuration from settings.xml

Maven's [settings.xml](https://maven.apache.org/settings.html) contains items that are not specific
to a project or that should not be distributed to artifact consumers. This plugin will read the
[servers](https://maven.apache.org/settings.html#servers) element of settings.xml to find a server
element with an `<id>` element that matches the registry. If found, the `<username>` and
`<password>` from that server element will be used. (The password is
[decrypted](https://maven.apache.org/guides/mini/guide-encryption.html) if needed.)

### Login Configuration from pom.xml

| Parameter | Required |        Property | Description                                            |
|----------:|:--------:|----------------:|--------------------------------------------------------|
|  registry |    ✓     | buildx.registry | Registry to authenticate with                          |
|  password |          | buildx.password | If registry not found in settings.xml, use as password |
|      skip |          |     buildx.skip | Skip login                                             |
|  username |          | buildx.username | If registry not found in settings.xml, use as username |

## Containerfile Goal

The [containerfile](https://chonton.github.io/buildx-maven-plugin/containerfile-mojo.html) goal
binds by default to the **prepare-package** phase.
This goal creates *${project.build.directory}/context/Containerfile* from the configuration.
See [Containerfile](https://github.com/containers/common/blob/main/docs/Containerfile.5.md) for more
information about Containerfile syntax.

### Containerfile Configuration

|     Parameter |             Property | Description                                                                            |
|--------------:|---------------------:|:---------------------------------------------------------------------------------------|
|           cli |           buildx.cli | Container command, default is **docker**. (**podman** is supported)                    |
| containerFile | buildx.containerFile | Instruction file relative to context, default is **Dockerfile** (or **Containerfile**) |
|       context |       buildx.context | Directory with build content, default is `${project.build.directory}/context`          |
|      contexts |                      | Map of additional context names to locations                                           |                                                                                         |
|           cmd |           buildx.cmd | Default [ShellOrExec Config](#shellorexec-config) command                              |
|    entrypoint |                      | Default [ShellOrExec Config](#shellorexec-config) entrypoint                           |
|          from |          buildx.from | Base image for subsequent instructions                                                 |
|        layers |                      | List of [Layer Config](#layer-config) to apply                                         |
|        labels |                      | Map of labels to apply to image                                                        |
|           env |                      | Map of environment variables that are set when container runs                          |
|          user |                      | User\[:Group] that runs inside the container. May be uid or name                       |
|        expose |                      | List of ports that the container will expose                                           |
|       volumes |                      | List of locations in the image filesystem for external mounts                          |
|       workDir |                      | Working directory for the container's process                                          |

### Layer Config

| Parameter | Required | Description                                                               |
|----------:|:--------:|:--------------------------------------------------------------------------|
|     chown |          | Owner\[:Group] of the files in the image                                  |
|     chmod |          | Permissions of the files in the image                                     |
|      srcs |    ✓     | Files relative to the context to be copied.  (golang wildcards supported) |
|      dest |    ✓     | Absolute destination in the image where files are copied                  |

### ShellOrExec Config

| Parameter |  Required  | Description                                                      |
|----------:|:----------:|:-----------------------------------------------------------------|
|      exec | ✓ or shell | List of Executable and parameters, no shell involved             |
|     shell |  ✓ or cmd  | Single line command executed by shell (unused if exec specified) |

## Build Goal

The [build](https://chonton.github.io/buildx-maven-plugin/build-mojo.html) goal binds by
default to the **package** phase. This goal executes `docker build buildx`.

### Build Configuration

|      Parameter |                Property | Description                                                                                |
|---------------:|------------------------:|:-------------------------------------------------------------------------------------------|
| buildArguments |                         | Map of build arguments                                                                     |
|        builder |          buildx.builder | Name of an already defined docker buildx builder                                           |
|            cli |              buildx.cli | Container command, default is **docker**. (**podman** is supported)                        |
|  containerFile |    buildx.containerFile | Instruction file relative to context, default is **Dockerfile** (or **Containerfile**)     |
|        context |          buildx.context | Directory with build content, default is `${project.build.directory}/context`              |
|       contexts |                         | Map of additional context names to locations                                               |
|          image |            buildx.image | Comma separated, fully qualified image name(s); must include *registry/repository:version* |
|           load |             buildx.load | If set to true, load the local docker image cache with resulting image                     |
|      platforms |                         | List of platforms.  Each element may contain comma separated *os/arch*                     |
|     provenance |       buildx.provenance | Level of provenance attestation to add. May be false, min, or max                          |
|           sbom |             buildx.sbom | Add software bill of materials attestation to add                                          |
|           skip |             buildx.skip | Skip build                                                                                 |

## Push Goal

The [push](https://chonton.github.io/buildx-maven-plugin/push-mojo.html) goal binds by
default to the **deploy** phase. This goal uses `docker buildx` to push an image to its registry.

### Push Configuration

| Parameter |        Property | Description                                                                                |
|----------:|----------------:|:-------------------------------------------------------------------------------------------|
|     image |    buildx.image | Comma separated, fully qualified image name(s), each in form *registry/repository:version* |
|      skip |     buildx.skip | Skip push                                                                                  |
|  skipPush | buildx.skipPush | buildx.skip                                                                                |                                         |

# Examples

## Typical Use

```xml

<build>
  <properties>
    <!-- override with -D build.platform=linux/amd64,linux/arm64 for multi-architecture build -->
    <build.platform/>
  </properties>

  <pluginManagement>
    <plugins>
      <plugin>
        <groupId>org.honton.chas</groupId>
        <artifactId>buildx-maven-plugin</artifactId>
        <version>0.0.9</version>
      </plugin>
    </plugins>
  </pluginManagement>

  <plugins>
    <plugin>
      <groupId>org.honton.chas</groupId>
      <artifactId>buildx-maven-plugin</artifactId>
      <executions>

        <execution>
          <id>build-java-based-containerfile</id>
          <goals>
            <goal>containerfile</goal>
          </goals>
          <configuration>
            <from>registry.access.redhat.com/ubi8/openjdk-11:1.15</from>
            <layers>
              <layer>
                <chown>nobody</chown>
                <srcs>
                  <src>target/quarkus-app/lib/</src>
                </srcs>
                <dest>/deployments/lib/</dest>
              </layer>
              <layer>
                <chown>nobody</chown>
                <srcs>
                  <src>target/quarkus-app/*.jar</src>
                </srcs>
                <dest>/deployments/</dest>
              </layer>
              <layer>
                <chown>nobody</chown>
                <srcs>
                  <src>target/quarkus-app/app/</src>
                </srcs>
                <dest>/deployments/app/</dest>
              </layer>
              <layer>
                <chown>nobody</chown>
                <srcs>
                  <src>target/quarkus-app/quarkus/</src>
                </srcs>
                <dest>/deployments/quarkus/</dest>
              </layer>
            </layers>
            <expose>
              <port>8080</port>
            </expose>
            <workdir>/work/</workdir>
            <user>nobody</user>
          </configuration>
        </execution>

        <execution>
          <id>build-java-based-container</id>
          <goals>
            <goal>build</goal>
            <goal>login</goal>
            <goal>push</goal>
          </goals>
          <configuration>
            <context>target/quarkus-app</context>
            <image>artifactory.example.com/dev/${project.artifactId}:${project.version}</image>
            <platforms>
              <platform>${build.platforms}</platform>
            </platforms>
          </configuration>
        </execution>

      </executions>

    </plugin>
  </plugins>
</build>
```
