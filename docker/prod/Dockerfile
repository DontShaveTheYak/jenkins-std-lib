FROM docker.io/jenkins/jenkins:2.346.3-lts-jdk11

ARG USERNAME=jenkins
ARG USER_UID=1000
ARG USER_GID=$USER_UID
ARG S6_OVERLAY_VERSION=3.1.2.1

USER root

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y \
    xz-utils \
    kmod \
    wget \
    git \
    curl \
    build-essential \
    python3 && \
    rm -rf /var/lib/apt/lists/*

# Install and setup docker into the container
RUN curl -fsSL https://get.docker.com -o get-docker.sh && \
    sh get-docker.sh

# This crazy stuff was needed to make docker work indepently inside the running container
# but also work with non root user like Jenkins... has to be a cleaner way tbh.
RUN wget -P /usr/bin/ https://raw.githubusercontent.com/docker-library/docker/master/dockerd-entrypoint.sh && \
    wget -P /usr/bin/ https://raw.githubusercontent.com/docker-library/docker/master/docker-entrypoint.sh && \
    chmod +x /usr/bin/dockerd-entrypoint.sh && \
    chmod +x /usr/bin/docker-entrypoint.sh && \
    mkdir -p /run/user/$USER_UID && \
    chown ${USER_GID}:${USER_GID} /run/user/$USER_UID && \
    update-alternatives --set iptables /usr/sbin/iptables-legacy && \
    update-alternatives --set ip6tables /usr/sbin/ip6tables-legacy && \
    touch /var/run/docker.sock && \
    chown root:docker /var/run/docker.sock && \
    chmod 660 /var/run/docker.sock && \
    usermod -aG docker $USERNAME

ADD https://github.com/just-containers/s6-overlay/releases/download/v${S6_OVERLAY_VERSION}/s6-overlay-noarch.tar.xz /tmp
RUN tar -C / -Jxpf /tmp/s6-overlay-noarch.tar.xz
ADD https://github.com/just-containers/s6-overlay/releases/download/v${S6_OVERLAY_VERSION}/s6-overlay-x86_64.tar.xz /tmp
RUN tar -C / -Jxpf /tmp/s6-overlay-x86_64.tar.xz

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

USER root
COPY docker/prod/root /

VOLUME [ "/var/lib/docker" ]

ENTRYPOINT ["/init"]
