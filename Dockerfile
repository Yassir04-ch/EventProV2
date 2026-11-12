FROM jenkins/jenkins:lts

USER root

RUN apt-get update \
    && apt-get install -y --no-install-recommends maven git \
    && rm -rf /var/lib/apt/lists/*

COPY --from=eclipse-temurin:17-jdk /opt/java/openjdk /opt/java17

ENV JAVA17_HOME=/opt/java17

USER jenkins
