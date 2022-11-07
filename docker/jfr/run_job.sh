#!/usr/bin/env bash

JOB=$1

/app/bin/jenkinsfile-runner run -w /usr/share/jenkins \
    --withInitHooks /usr/share/jenkins/ref/init.groovy.d \
    -p /usr/share/jenkins/ref/plugins \
    --runWorkspace /var/jenkins_home/workspace/job \
    -f "/workspace/${JOB}"
