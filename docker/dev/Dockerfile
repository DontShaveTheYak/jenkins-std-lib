ARG baseImage=dsty/jenkins

FROM ${baseImage}

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y \
        python3 \
        python3-pip && \
    python3 -m pip install --upgrade pip && \
    rm -rf /var/lib/apt/lists/*

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
ENV PATH="/root/.local/bin:$PATH"
