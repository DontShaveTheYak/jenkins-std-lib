FROM jenkins/jenkinsfile-runner

COPY jenkins.yaml /usr/share/jenkins/ref/jenkins.yaml
COPY init_scripts/* /usr/share/jenkins/ref/init.groovy.d/

ENV CASC_JENKINS_CONFIG /usr/share/jenkins/ref/jenkins.yaml

# ENTRYPOINT ["/app/bin/jenkinsfile-runner-launcher"]



# ENTRYPOINT ["/app/bin/jenkinsfile-runner",\
#             "-w", "/app/jenkins",\
#             "-p", "/usr/share/jenkins/ref/plugins",\
#             "-f", "/workspace/Jenkinsfile"]