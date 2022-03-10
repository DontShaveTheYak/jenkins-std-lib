/* groovylint-disable, DuplicateMapLiteral, DuplicateStringLiteral, UnusedVariable */
@Library('jenkins-std-lib')

import org.dsty.github.actions.Workflow

node() {
    // Ignore this line its for catching CPS issues.
    String cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    stage('Checkout code') {
        checkout(
            changelog: false,
            poll: false,
            scm: [
                $class: 'GitSCM',
                branches: [[name: 'master']],
                extensions: [],
                userRemoteConfigs: [[url: 'https://github.com/cplee/github-actions-demo.git']]
            ]
        )
    }

    stage('Run Workflow') {
        Workflow workflow = new Workflow(this)

        String output = workflow()
    }

    // Ignore this line its for catching CPS issues.
    cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}
