FROM jenkins/jenkins:latest-jdk11

# Install docker into the container
USER root
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y \
        wget \
        git \
        curl \
        python3 \
        python3-pip && \
    python3 -m pip install --upgrade pip && \
    rm -rf /var/lib/apt/lists/*

# Install and setup docker into the container
RUN groupadd -g 118 docker && curl -fsSL https://get.docker.com -o get-docker.sh && \
    sh get-docker.sh && usermod -aG docker jenkins

USER jenkins

# Install pre-commit and dependencies
COPY tests/requirements.txt /tmp
RUN pip3 install --no-cache-dir -r /tmp/requirements.txt

## Plugins
COPY --chown=jenkins:jenkins docker/*-plugins.txt /usr/share/jenkins/ref/
RUN cat /usr/share/jenkins/ref/lib-plugins.txt >> /usr/share/jenkins/ref/runner-plugins.txt && \
    jenkins-plugin-cli -f /usr/share/jenkins/ref/runner-plugins.txt && \
    jenkins-plugin-cli --plugins job-dsl build-blocker-plugin

## Setup init scripts
COPY --chown=jenkins:jenkins .devcontainer/*.groovy docker/init_scripts/src/main/groovy/* /usr/share/jenkins/ref/init.groovy.d/

ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false
ENV JENKINS_SLAVE_AGENT_PORT=
ENV JENKINS_OPTS="--httpPort=80"
ENV PATH="/var/jenkins_home/.local/bin:$PATH"
