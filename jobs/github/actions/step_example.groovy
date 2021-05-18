/* groovylint-disable DuplicateMapLiteral, DuplicateStringLiteral, UnnecessaryParenthesesForMethodCallWithClosure, UnusedVariable */
@Library('pipeline-library')

import org.dsty.github.actions.Step

node() {

    String cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    Step action = new Step(this)

    Map options = [
        'name': 'Get build time.',
        'uses': 'actions/hello-world-docker-action@master',
        'with': [
            'who-to-greet': 'Mona the Octocat'
        ]
    ]

    Map outputs = action(options)

    if (!outputs.time) {
        error('Should have an output named time.')
    }

    cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}
