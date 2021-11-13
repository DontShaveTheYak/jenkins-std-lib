# Contributing to Jenkins-Std-Lib

A big welcome and thank you for considering contributing to DontShaveTheYak (dsty) open source projects! It’s people like you that help reduce [Yak Shaving](https://seths.blog/2005/03/dont_shave_that/) in the DevOps community.

Reading and following these guidelines will help us make the contribution process easy and effective for everyone involved. It also communicates that you agree to respect the time of the developers managing and developing these open source projects. In return, we will reciprocate that respect by addressing your issue, assessing changes, and helping you finalize your pull requests.

## Getting Started

Contributions are made to this repo via Issues and Pull Requests (PRs). A few general guidelines that cover both:

- Search for existing Issues and PRs before creating your own.
- We work hard to makes sure issues are handled in a timely manner but, depending on the impact, it could take a while to investigate the root cause. A friendly ping in the comment thread to the submitter or a contributor can help draw attention if your issue is blocking.

### Issues

Issues should be used to report problems with the library, request a new feature, or to discuss potential changes before a PR is created.

If you find an Issue that addresses the problem you're having, please add your own reproduction information to the existing issue rather than creating a new one. Adding a [reaction](https://github.blog/2016-03-10-add-reactions-to-pull-requests-issues-and-comments/) can also help be indicating to our maintainers that a particular problem is affecting more than just the reporter.

### Pull Requests

PRs to our libraries are always welcome and can be a quick way to get your fix or improvement slated for the next release. In general, PRs should:

- Only fix/add the functionality in question **OR** address wide-spread whitespace/style issues, not both.
- Add unit or integration tests for fixed or changed functionality (if a test suite already exists).
- Address a single concern in the least number of changed lines as possible.
- Include javadoc documentation in the repo.

In general, we follow the ["fork-and-pull" Git workflow](https://github.com/susam/gitpr)

1. Fork the repository to your own Github account
2. Clone the project to your machine
3. Create a branch locally with a succinct but descriptive name
4. Commit changes to the branch
5. Following any formatting and testing guidelines specific to this repo
6. Push changes to your fork
7. Open a PR in our repository so that we can efficiently review the changes.

### Code Style
We are using GroovyLint to create a consistent experience when reading the source code. We do often ignore certain rules from GroovyLint like getter/setter rules so feel free to ignore a rule that you don't think applies.

### Setup
The testing of this library is a little quirky. We currently test the library by creating Jenkins jobs in the [jobs](./jobs) folder. The format is `jobs/${packageName}/${className}_example.groovy` and this serves a basic example for users on how to use the class. You can also create unit tests using this format `jobs/${packageName}/tests/test_${className}.groovy`

We then use pytest to call a docker image that will run the jenkins job and return the output of the job. Pytest files use the following format `tests/test_${packageName}/test_${className}.py`

## Development environment

We highly recommend the development environment we have setup for [vscode](https://code.visualstudio.com/). This development environment contains all the tooling and dependencies you need to contribute to this project and will save you hours of time setting up these items manually.

### vscode

The requirements for using the vscode dev environment is to have the [remote-containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) extension installed.

When you first open this repository in vscode you will get a notification that this workspace contains a dev container. Click "**Reopen in Container**"

If you miss the notification you can manually open the workspace in a remote container by opening up the command palette `CTRL+SHIFT+P` and type `Open Workspace in Container`.

Note: The first time you open the workspace using the remote-container it will take 5-10mins to configure the development environment. The next time you use the remote-container it will open much faster.

Once opened in the dev container you can:
* Run linting with `pre-commit run -a`
* Open `http://localhost:5050` in your browser to access the Jenkins UI and run tests manually. When you click build on a job, it will automaticly use the latest job/library code.
* Run all the tests in Jenkins UI automaticlly with `pytest -s`

### Manual

If you are not using docker or vscode you can setup a development environment using the following steps:

1. Have python (we use 3.9) installed and you should probably setup a venv. [Pyenv Guide](https://switowski.com/blog/pyenv)
2. Install the python requirements.
    ```
    python -m pip install tests/requirements.txt
    ```
3. Install pre-commit hooks.
    ```
    pre-commit install
    ```
4. Run pre-commit to test everything is setup correctly.
    ```
    pre-commit run --all-files
    ```
5. Make your changes and add or update tests.
6. Run pytest
    ```
    pytest -s
    ```
7. Once all tests are passing commit your changes and open a PR.
