/* groovylint-disable, DuplicateMapLiteral, DuplicateStringLiteral, UnusedVariable */
@Library('jenkins-std-lib')

import org.dsty.github.actions.Step

node() {

    String cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    Step action = new Step(this)

    Map options

    Map outputs

    stage('Docker Action') {
        options = [
            'name': 'Test Docker Action',
            'uses': 'actions/hello-world-docker-action@master',
            'with': [
                'who-to-greet': 'DockerAction'
            ]
        ]

        outputs = action(options)

        if (!outputs.time) {
            error('Should have an output named time.')
        }
    }

    stage('JavaScript Action') {
        options = [
            'name': 'Test JavaScript Action',
            'uses': 'actions/hello-world-javascript-action@main',
            'with': [
                'who-to-greet': 'JavaScriptAction'
            ]
        ]

        outputs = action(options)

        if (!outputs.time) {
            error('Should have an output named time.')
        }
    }

    stage('Run Action') {
        options = [
            'name': 'Test RunAction.',
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
    }

    cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}
