
FROM jenkins/jenkins:lts

USER root

# Installer Maven, Git et Curl
RUN apt-get update \
    && apt-get install -y --no-install-recommends maven git curl \
    && rm -rf /var/lib/apt/lists/*

# Installer Docker CLI
RUN curl -fsSL https://download.docker.com/linux/static/stable/x86_64/docker-29.0.0.tgz \
    | tar -xz --strip-components=1 -C /usr/local/bin docker/docker

# Installer Docker Compose CLI Plugin
RUN mkdir -p /usr/local/lib/docker/cli-plugins \
    && curl -fSL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 \
       -o /usr/local/lib/docker/cli-plugins/docker-compose \
    && chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# Installer Java 17
COPY --from=eclipse-temurin:17-jdk /opt/java/openjdk /opt/java17

ENV JAVA17_HOME=/opt/java17

USER jenkins
