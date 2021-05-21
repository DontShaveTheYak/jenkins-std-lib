/* groovylint-disable DuplicateMapLiteral, DuplicateStringLiteral, UnnecessaryParenthesesForMethodCallWithClosure, UnusedVariable */
@Library('pipeline-library')

import org.dsty.github.actions.Step

node() {

    // This is needed if you run jenkins in a docker container.
    // It's the path on the host machine where your docker bind mount is stored.
    // docker run -v '/tmp/jenkins_home:/var/run/jenkins_home' jenkins/jenkins:lts
    env.DIND_JENKINS_HOME = '/tmp/jenkins_home'

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
            'uses': 'actions/hello-world-javascript-action@master',
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
