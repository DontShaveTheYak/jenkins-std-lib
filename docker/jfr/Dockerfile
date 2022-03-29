ARG baseImage=dsty/jenkins

FROM jenkins/jenkinsfile-runner:latest as jfr

FROM ${baseImage}

COPY --from=jfr /app /app

USER root

RUN cd /usr/share/jenkins && jar -xvf jenkins.war

ENV JENKINS_HOME="/usr/share/jenkins/ref/"
ENV JAVA_OPTS="-Djenkins.model.Jenkins.slaveAgentPort=50000 -Djenkins.model.Jenkins.slaveAgentPortEnforce=true -Dhudson.model.LoadStatistics.clock=1000"

USER jenkins

ENTRYPOINT ["/app/bin/jenkinsfile-runner", "-w", "/usr/share/jenkins/", "-p", "/usr/share/jenkins/ref/plugins", "--withInitHooks", "/usr/share/jenkins/ref/init.groovy.d/", "-f"]

CMD ["/workspace/Jenkinsfile"]
