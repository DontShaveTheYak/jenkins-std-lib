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

## Plugins
RUN jenkins-plugin-cli --plugins workflow-aggregator filesystem_scm git

## Setup init scripts
COPY docker/prod/init_scripts/* /usr/share/jenkins/ref/init.groovy.d/

ENV JENKINS_SLAVE_AGENT_PORT=
ENV JENKINS_OPTS="--httpPort=80"
