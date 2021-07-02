FROM jenkins/jenkinsfile-runner:latest

RUN apt-get update && apt-get install -y wget git curl sudo && rm -rf /var/lib/apt/lists/* && curl -fsSL https://get.docker.com -o get-docker.sh && sh get-docker.sh

## Plugins
COPY *-plugins.txt /usr/share/jenkins/ref/
RUN cd /app/jenkins && jar -cvf jenkins.war * && cat /usr/share/jenkins/ref/lib-plugins.txt >> /usr/share/jenkins/ref/runner-plugins.txt
RUN java -jar /app/bin/jenkins-plugin-manager.jar --war /app/jenkins/jenkins.war --plugin-file /usr/share/jenkins/ref/runner-plugins.txt && rm /app/jenkins/jenkins.war

VOLUME /usr/share/jenkins/ref/casc

COPY jenkins.yaml /usr/share/jenkins/ref/jenkins.yaml
COPY init_scripts/src/main/groovy/* /usr/share/jenkins/ref/init.groovy.d/

ENV JENKINS_HOME="/usr/share/jenkins/ref/"
ENV JAVA_OPTS="-Djenkins.model.Jenkins.slaveAgentPort=50000 -Djenkins.model.Jenkins.slaveAgentPortEnforce=true -Dhudson.model.LoadStatistics.clock=1000"
ENV CASC_JENKINS_CONFIG /usr/share/jenkins/ref/jenkins.yaml

ENTRYPOINT ["/app/bin/jenkinsfile-runner",\
            "-w", "/usr/share/jenkins/",\
            "-p", "/usr/share/jenkins/ref/plugins"]

CMD ["-f /workspace/Jenkinsfile"]
