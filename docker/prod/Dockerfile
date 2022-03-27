FROM jenkins/jenkins:latest-jdk11

USER root

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y \
        wget \
        git \
        curl && \
    rm -rf /var/lib/apt/lists/*

# Install and setup docker into the container
RUN curl -fsSL https://get.docker.com -o get-docker.sh && \
    sh get-docker.sh

USER jenkins

WORKDIR $JENKINS_HOME

## Plugins for container
RUN jenkins-plugin-cli --plugins filesystem_scm git

## Plugins required for library
COPY docker/prod/plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt

## Setup init scripts
COPY docker/prod/init_scripts/* /usr/share/jenkins/ref/init.groovy.d/

ENV JENKINS_SLAVE_AGENT_PORT=
ENV JENKINS_OPTS="--httpPort=80"

EXPOSE 80
