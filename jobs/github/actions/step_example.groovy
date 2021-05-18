/* groovylint-disable DuplicateMapLiteral, DuplicateStringLiteral, UnnecessaryParenthesesForMethodCallWithClosure, UnusedVariable */
@Library('pipeline-library')

import org.dsty.github.actions.Step

node() {

    String cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    Step action = new Step(this)

    Map options = [
        'name': 'Test Docker Action',
        'uses': 'actions/hello-world-docker-action@master',
        'with': [
            'who-to-greet': 'Mona the Octocat'
        ]
    ]

    Map outputs = action(options)

    if (!outputs.time) {
        error('Should have an output named time.')
    }

    options = [
        'name': 'Test Run Action.',
        'run': '''\
            echo "Setting an output!"
            echo "::set-output name=test::SomeValue"
        '''
    ]

    outputs = action(options)

    if (!outputs.test) {
        error('Should have an output named test.')
    }

    if (outputs.test != 'SomeValue') {
        error('Should set the correct output Value.')
    }

    cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}
