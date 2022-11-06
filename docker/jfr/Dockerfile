ARG baseImage=dsty/jenkins

FROM jenkins/jenkinsfile-runner:latest as jfr

FROM ${baseImage}

COPY --from=jfr /app /app

USER root

RUN cd /usr/share/jenkins && jar -xvf jenkins.war

# We dont font need to run the Jenkins GUI in the JFR
RUN rm /etc/s6-overlay/s6-rc.d/user/contents.d/jenkins

COPY /docker/jfr/run_job.sh /usr/bin/run_job

ENV JENKINS_HOME="/usr/share/jenkins/ref/"
