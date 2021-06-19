/* groovylint-disable, DuplicateMapLiteral, DuplicateStringLiteral, UnusedVariable */
@Library('jenkins-std-lib')

import org.dsty.github.actions.Step

node() {
    String cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    Step action = new Step(this)

    Map options = [
      'name': 'TestIllegalArgument'
    ]

    try {
        Map outputs = action(options)
        error("Should not run if 'uses' or 'run' not provided.")
    } catch (IllegalArgumentException ex) {
        println('Threw the correct exception.')
    }

    options = [
      'name': 'TestConditional',
      'uses': 'actions/hello-world-docker-action@master',
      'with': [
        'who-to-greet': 'Mona the Octocat'
      ],
      'if': false
    ]

    Map outputs = action(options)

    if (outputs) {
        error('Should haved skipped running the step.')
    }

    cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}
