/* groovylint-disable DuplicateMapLiteral, DuplicateStringLiteral, UnusedVariable */
@Library('jenkins-std-lib')

import org.dsty.scm.Generic

node() {
    String cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    // If you are not sure if you are dealing with git or svn then Generic is a good choice.
    Generic scmClient = new Generic(this)

    // These are the same options you would pass to the checkout step
    // https://www.jenkins.io/doc/pipeline/steps/workflow-scm-step/

    String defaultBranch = 'master'

    Map options = [
        changelog: false,
        poll: false,
        scm: [
            $class: 'GitSCM',
            branches: [[name: defaultBranch]],
            extensions: [],
            userRemoteConfigs: [[url: 'https://github.com/DontShaveTheYak/jenkins-std-lib.git']]
        ]
    ]

    scmClient.checkout(options)

    if (!env.GIT_BRANCH.contains(defaultBranch)) {
        error("${env.GIT_BRANCH} should be equal to ${defaultBranch}.")
    }

    options.scm.branches = [[name: 'develop']]

    dir('testing') {
        scmClient.withCheckout(options) {
            if (env.GIT_BRANCH.contains(defaultBranch)) {
                error("${env.GIT_BRANCH} should not equal ${defaultBranch}.")
            }
        }
    }

    if (!env.GIT_BRANCH.contains(defaultBranch)) {
        error("${env.GIT_BRANCH} should be equal to ${defaultBranch}.")
    }

    cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}
