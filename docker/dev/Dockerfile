ARG baseImage=dsty/jenkins

FROM ${baseImage}

ARG NONROOT_USER=jenkins
ARG DEBIAN_FRONTEND=noninteractive

USER root

ARG USERNAME=jenkins
ARG USER_UID=1000
ARG USER_GID=$USER_UID

# Ensure apt is in non-interactive to avoid prompts
ENV DEBIAN_FRONTEND=noninteractive

# # Create the user
RUN apt-get update \
    && apt-get install -y sudo \
    && echo $USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME \
    && chmod 0440 /etc/sudoers.d/$USERNAME

RUN apt-get update && \
    apt-get install -y \
        python3 \
        python3-pip && \
    python3 -m pip install --upgrade pip && \
    rm -rf /var/lib/apt/lists/*

USER jenkins
WORKDIR $JENKINS_HOME

# Install pre-commit and dependencies
COPY tests/requirements.txt /tmp
RUN pip3 install --no-cache-dir -r /tmp/requirements.txt

## Plugins
RUN jenkins-plugin-cli --plugins job-dsl

## Setup init scripts
COPY docker/dev/init_scripts/* /usr/share/jenkins/ref/init.groovy.d/

ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false
ENV JENKINS_SLAVE_AGENT_PORT=
ENV JENKINS_OPTS="--httpPort=80"
ENV PATH="$JENKINS_HOME/.local/bin:$PATH"
