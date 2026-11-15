FROM jenkins/jenkins:lts

USER root

RUN apt-get update \
    && apt-get install -y --no-install-recommends maven git curl \
    && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL https://download.docker.com/linux/static/stable/x86_64/docker-29.0.0.tgz \
    | tar -xz --strip-components=1 -C /usr/local/bin docker/docker

COPY --from=eclipse-temurin:17-jdk /opt/java/openjdk /opt/java17

ENV JAVA17_HOME=/opt/java17

USER jenkins