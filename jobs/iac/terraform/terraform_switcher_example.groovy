/* groovylint-disable, DuplicateMapLiteral, DuplicateStringLiteral, UnusedVariable */
@Library('jenkins-std-lib')

import org.dsty.logging.LogClient
import org.dsty.system.os.Path
import org.dsty.system.os.programs.CliTool
import org.dsty.iac.terraform.TerraformSwitcher

LogClient log = new LogClient()

node() {
    // Ignore this line its for catching CPS issues.
    String cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    log.info('Lets install the latest version of tfswitch.')

    TerraformSwitcher tfswitch = new TerraformSwitcher()

    // Note that tfswitch wont be downloaded until we try to use it.
    tfswitch.call('-v')

    log.info('We can use tfswitch to install Terraform. Lets start my specifying a specific version:')

    CliTool tf = tfswitch.install('0.11.0')

    tf.runCommand('-v')

    log.info('We can also get the latest version:')

    tf = tfswitch.installLatest()

    tf.runCommand('-v')

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

    log.info('Then that can be used by tfswitch to automaticly install the correct version of terraform.')

    Path ws = Path.workspace()

    Path tfVersionFile = ws.child('version.tf')

    ws.withTempFile(tfVersionFile) {

        tfVersionFile.write(tfVersion)

        tf = tfswitch.install()

        tf.runCommand('-v')

    }

    // Ignore this line its for catching CPS issues.
    cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}
