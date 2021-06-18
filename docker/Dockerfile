FROM jenkins/jenkinsfile-runner:build-mvncache as jfr-mvncache

###
# Build stage
###
FROM maven:3.5.4 as jfr-build
ENV MAVEN_OPTS=-Dmaven.repo.local=/mavenrepo
COPY --from=jfr-mvncache /mavenrepo /mavenrepo
ADD pom.xml /jenkinsfile-runner/pom.xml
RUN cd /jenkinsfile-runner && mvn clean package
RUN mkdir /app && unzip /jenkinsfile-runner/target/war/jenkins.war -d /app/jenkins && \
  rm -rf /app/jenkins/scripts /app/jenkins/jsbundles /app/jenkins/css /app/jenkins/images /app/jenkins/help /app/jenkins/WEB-INF/detached-plugins /app/jenkins/winstone.jar /app/jenkins/WEB-INF/jenkins-cli.jar /app/jenkins/WEB-INF/lib/jna-4.5.2.jar

####
# Production image
####
FROM adoptopenjdk:8u262-b10-jdk-hotspot

LABEL Description="This is a base image for a single-shot ci.jenkins.io demo" Vendor="Oleg Nenashev" Version="0.3"

# Packages
RUN apt-get update && apt-get install -y wget git curl sudo && rm -rf /var/lib/apt/lists/* && curl -fsSL https://get.docker.com -o get-docker.sh && sh get-docker.sh

COPY --from=jfr-build /jenkinsfile-runner/target/appassembler /app
COPY --from=jfr-build /jenkinsfile-runner/target/plugins /usr/share/jenkins/ref/plugins
COPY --from=jenkins/jenkinsfile-runner:1.0-beta-15 /app/bin/jenkinsfile-runner-launcher /app/bin/jenkinsfile-runner-launcher
# /app/jenkins is a location of the WAR file. It can be empty in the current packaging
RUN mkdir /app/jenkins

VOLUME /usr/share/jenkins/ref/casc

ENV JENKINS_HOME="/usr/share/jenkins/ref/"
ENV JAVA_OPTS="-Djenkins.model.Jenkins.slaveAgentPort=50000 -Djenkins.model.Jenkins.slaveAgentPortEnforce=true -Dhudson.model.LoadStatistics.clock=1000"
ENV CASC_JENKINS_CONFIG /usr/share/jenkins/ref/jenkins.yaml
COPY jenkins.yaml /usr/share/jenkins/ref/jenkins.yaml
COPY init_scripts/src/main/groovy/* /usr/share/jenkins/ref/init.groovy.d/

ENTRYPOINT ["/app/bin/jenkinsfile-runner",\
            "-w", "/app/jenkins",\
            "-p", "/usr/share/jenkins/ref/plugins",\
            "-f", "/workspace/Jenkinsfile"]
