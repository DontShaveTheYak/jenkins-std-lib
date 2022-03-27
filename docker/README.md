# Docker
This folder contains the various containers used by the Jenkins Standard Library.

* [prod](./prod/) - This container is based on [jenkins/jenkins:latest-jdk11](https://hub.docker.com/r/jenkins/jenkins) and has the Jenkins Standard Library already configured.
* [dev](./dev/) - Based on the [prod](./prod/) image but has additional configuration for local development using vscode dev containers.
* [jfr](./jfr/) - Based on the [prod](./prod/) image but is setup as a [Jenkinsfile-Runner](https://github.com/jenkinsci/jenkinsfile-runner) to run tests.
* [demo](./demo/) - Based on the [prod](./prod/) image and setup with examples of
