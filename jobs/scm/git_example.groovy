/* groovylint-disable DuplicateMapLiteral, DuplicateStringLiteral, UnnecessaryGetter, UnusedVariable */
@Library('jenkins-std-lib')

import org.dsty.scm.Git

node() {
    String cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    // When you know you are using Git.
    Git git = new Git(this)

    // These are the same options you would pass to the checkout step
    // https://www.jenkins.io/doc/pipeline/steps/workflow-scm-step/

    git.checkout(
        changelog: false,
        poll: false,
        scm: [
            $class: 'GitSCM',
            branches: [[name: '0.7.0']],
            extensions: [],
            userRemoteConfigs: [[url: 'https://github.com/DontShaveTheYak/jenkins-std-lib.git']]
        ]
    )

    Map envVars = getEnvVars()

    Map gitVars = envVars.findAll { k, v ->
        k.contains('GIT')
    }

    if (gitVars.size() < 3) {
        // The number will depend on the plugins you have.
        // It should at least set GIT_COMMIT, GIT_BRANCH and GIT_URL
        error('Should set GIT_* Environment variables.')
    }

    git.changeBranch('0.6.3')

    if (env.GIT_BRANCH != '0.6.3') {
        error('Should update Environment variables when changing branches.')
    }

    git.withBranch('master') {
        if (env.GIT_BRANCH != 'master') {
            error('Should update Environment variables when temporaily changing branch.')
        }
    }

    List<String> updatedFiles = git.changedFiles(target:'0.7.0')

    if (updatedFiles.size() != 27) {
        error('Should return the files that changed.')
    }

    List<String> localBranches = git.localBranches()

    if (!localBranches.contains('master')) {
        error('Should have the master branch locally.')
    }

    List<String> remoteBranches = git.remoteBranches()

    if (!remoteBranches.contains('origin/develop')) {
        error('Should have the develop branch on the remote.')
    }

    List<String> tags = git.tags()

    if (!tags.contains('0.2.1')) {
        error('Should have the tag 0.2.1.')
    }

    cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}

Map getEnvVars() {
    String result = sh(script: 'printenv | paste -sd "," -', returnStdout: true).trim()
    List<String> vars = result.split(',')

    Map envVars = vars.collectEntries { String envVar ->
        List parts = envVar.split('=')
        [(parts[0]): parts[1]]
    }

    return envVars
}
