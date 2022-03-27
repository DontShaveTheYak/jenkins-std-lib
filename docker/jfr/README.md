# DSTY Jenkinsfile-Runner

This image uses the [Jenkinsfile-Runner](https://github.com/jenkinsci/jenkinsfile-runner) to run jenkins in a "headless" state to execute tests both locally and in CI/CD.

## Building

This container and build and configured by [pytest](../../tests/conftest.py), which is usually called by `pre-commit` when commiting locally or being tested in CICD.
