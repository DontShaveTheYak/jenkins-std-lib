/* groovylint-disable DuplicateMapLiteral, DuplicateStringLiteral, UnnecessaryGetter, UnusedVariable */
@Library('jenkins-std-lib')

import org.dsty.jenkins.Build

node() {
    // CPS test can be ignored, it is not part of the example code.
    String cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    Build thisBuild = new Build()

    stage('WorkflowScript') {

        Object wfs = thisBuild.getWorkFlowScript()

        String pwd = wfs.pwd()

        if (pwd != env.WORKSPACE) {
            error('Should have returned the current PWD.')
        }
    }

    stage('EnvironmentVars') {
        Map<String, String> envVars = thisBuild.environmentVars()

        if (envVars.BUILD_TAG != env.BUILD_TAG) {
            error('Failed to fetch environment vars.')
        }
    }

    cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}
