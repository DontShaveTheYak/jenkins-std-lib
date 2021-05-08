/* groovylint-disable DuplicateMapLiteral, DuplicateStringLiteral, UnnecessaryParenthesesForMethodCallWithClosure, UnusedVariable */
@Library('pipeline-library')

import org.dsty.github.Action

node() {
    String cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    Action action = new Action(this)

    Map outputs = action.uses('actions/hello-world-docker-action@master').with(['who-to-greet': 'Jenkins-Std-Lib'])

    if (!outputs.time) {
        error('Should have an output named time.')
    }

    cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}
