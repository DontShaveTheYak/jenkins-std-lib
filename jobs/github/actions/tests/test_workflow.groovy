/* groovylint-disable, DuplicateMapLiteral, DuplicateStringLiteral, UnusedVariable */
@Library('jenkins-std-lib')

import org.dsty.github.actions.Workflow

node() {
    String cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    Workflow workflow = new Workflow(this)

    // Should install the latest
    workflow.install()

    String latestVersion = workflow.version

    // Set an older version to install
    workflow.version = 'v0.2.21'
    workflow.install()

    String olderVersion = workflow.version

    if (olderVersion == latestVersion) {
        error('Should have override the installed version.')
    }

    // Should use existing version
    workflow.version = ''
    workflow.install()

    if (olderVersion != workflow.version) {
        error('Should not have changed version.')
    }

    String output = workflow.run('--version')

    if (!output.contains(workflow.version.replace('v', ''))) {
        error('Should have ran the version command.')
    }

    cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}
