/* groovylint-disable DuplicateMapLiteral, DuplicateStringLiteral, UnnecessaryGetter, UnusedVariable */
@Library('jenkins-std-lib')
import org.dsty.system.Platform
import org.dsty.system.System

node() {
    // Ignore this line its for catching CPS issues.
    sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    // Get the current system type like WINDOWS or UNIX
    System currentSystem = Platform.system()

    List<String> validDistros = ['Ubuntu', 'CentOS', 'UNKNOWN']

    String currentDistro = currentSystem.distribution()

    if (!validDistros.contains(currentDistro)) {

        error("Should be a valid distribution but was ${currentDistro}")

    }

    // Ignore this line its for catching CPS issues.
    sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}
