/* groovylint-disable DuplicateMapLiteral, DuplicateStringLiteral, UnusedVariable */
@Library('pipeline-library')

import org.dsty.jenkins.Instance

node() {
    // CPS test can be ignored, it is not part of the example code.
    String cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    Instance jenkins = new Instance()

    stage('Get Installed Plugins') {
        List plugins = jenkins.plugins()

        if (!plugins) {
            error('Should have found plugins')
        }

        if (jenkins.pluginInstalled('fakePlugin')) {
            error('Should not find plugin.')
        }

        if (!jenkins.pluginInstalled('workflow-job')) {
            error('Should have plugin installed.')
        }
    }

    cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}
