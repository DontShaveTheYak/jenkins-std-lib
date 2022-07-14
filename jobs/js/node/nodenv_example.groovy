/* groovylint-disable, DuplicateMapLiteral, DuplicateStringLiteral, UnusedVariable */
@Library('jenkins-std-lib')

import org.dsty.logging.LogClient
import org.dsty.system.os.Path
import org.dsty.system.os.programs.CliTool
import org.dsty.js.node.NodeEnv

LogClient log = new LogClient()

node() {
    // Ignore this line its for catching CPS issues.
    String cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    log.info('Lets install the latest version of nodenv.')

    NodeEnv nodenv = new NodeEnv()

    // Note that nodenv wont be downloaded and installed until we try to use it.
    nodenv.call('-v')

    log.info('We can use nodenv to install a version of Node. Lets start my specifying a specific version:')

    CliTool node = nodenv.install('16.16.0')

    node.runCommand('-v')

    log.info('We can also get the latest version:')

    node = nodenv.installLatest()

    node.runCommand('-v')

    log.info('If your terraform project specifies a terraform version like this:')

    String tfVersion = '''\
terraform {
  required_version = ">= 0.12.9, < 0.15.0"

  required_providers {
    aws        = ">= 2.52.0"
    kubernetes = ">= 1.11.1"
  }
}
'''
    println tfVersion

    log.info('Then that can be used by nodenv to automaticly install the correct version of terraform.')

    Path ws = Path.workspace()

    Path tfVersionFile = ws.child('version.tf')

    ws.withTempFile(tfVersionFile) {

        tfVersionFile.write(tfVersion)

        node = nodenv.install()

        node.runCommand('-v')

    }

    // Ignore this line its for catching CPS issues.
    cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}
