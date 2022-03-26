ARG baseImage=dsty/jenkins

FROM ${baseImage}

ARG NONROOT_USER=jenkins
ARG DEBIAN_FRONTEND=noninteractive

USER root

RUN apt-get update && \
    apt-get install -y \
        sudo \
        python3 \
        python3-pip && \
    python3 -m pip install --upgrade pip && \
    rm -rf /var/lib/apt/lists/*

RUN echo "#!/bin/sh\n\
    sudoIf() { if [ \"\$(id -u)\" -ne 0 ]; then sudo \"\$@\"; else \"\$@\"; fi }\n\
    SOCKET_GID=\$(stat -c '%g' /var/run/docker.sock) \n\
    if [ \"${SOCKET_GID}\" != '0' ]; then\n\
        if [ \"\$(cat /etc/group | grep :\${SOCKET_GID}:)\" = '' ]; then sudoIf groupadd --gid \${SOCKET_GID} docker-host; fi \n\
        if [ \"\$(id ${NONROOT_USER} | grep -E \"groups=.*(=|,)\${SOCKET_GID}\(\")\" = '' ]; then sudoIf usermod -aG \${SOCKET_GID} ${NONROOT_USER}; fi\n\
    fi\n\
    exec \"\$@\"" > /usr/local/share/docker-init.sh \
    && chmod +x /usr/local/share/docker-init.sh \
    && echo $NONROOT_USER ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$NONROOT_USER \
    && chmod 0440 /etc/sudoers.d/$NONROOT_USER

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
ENV PATH="/$JENKINS_HOME/.local/bin:$PATH"

ENTRYPOINT [ "/usr/local/share/docker-init.sh" ]
CMD [ "sleep", "infinity" ]
