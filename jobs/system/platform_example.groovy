/* groovylint-disable DuplicateMapLiteral, DuplicateStringLiteral, UnnecessaryGetter, UnusedVariable */
@Library('jenkins-std-lib')
import org.dsty.system.Platform
import org.dsty.system.System
import org.dsty.system.os.shell.Shell

node() {
    // Ignore this line its for catching CPS issues.
    sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    // Get the current system type like WINDOWS or UNIX
    System currentSystem = Platform.system()

    List<String> validSystems = ['UNIX', 'WINDOWS', 'DARWIN']

    // These tests are generic because I cant be certain where
    // they will be run.
    if (!validSystems.contains(currentSystem.name())) {
        error("Should be a valid system but was ${currentSystem}")
    }

    // Get the current CPU architechture
    String architecture = Platform.architecture()

    List<String> validArchs = ['x86', 'x64', 'amd64', 'arm', 'arm64']

    if (!validArchs.contains(architecture)) {
        error("Should be a valid architecture but was ${architecture}")
    }

    // Get a Shell for the current system
    Shell shell = Platform.getShell()

    // Make sure the shell works by writting a msg with it.
    String msg = "Ran on ${architecture} ${currentSystem}"
    shell("echo ${msg}")

    // Ignore this line its for catching CPS issues.
    sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}
