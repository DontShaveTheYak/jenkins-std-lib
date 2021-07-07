/* groovylint-disable DuplicateMapLiteral, DuplicateStringLiteral, UnusedVariable */
@Library('jenkins-std-lib')

import org.dsty.scm.Generic

node() {
    String cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    Generic scmClient = new Generic(this)

    Map newEnvVars = ['KeyA': 'ValueA']

    scmClient.saveEnvironment(newEnvVars)


    Map currentVars = getEnvVars()

    newEnvVars.each { k, v ->
        if (!currentVars.containsKey(k) || !currentVars.containsValue(v)) {
            error("${k} or ${v} missing from environment variables.")
        }
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
