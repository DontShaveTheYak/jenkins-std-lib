/* groovylint-disable, DuplicateMapLiteral, DuplicateStringLiteral, UnusedVariable */
@Library('jenkins-std-lib')

import org.dsty.logging.LogClient
import org.dsty.system.os.Path
import org.dsty.system.os.programs.CliTool
import org.dsty.js.node.Node
import org.dsty.js.node.Npm

LogClient log = new LogClient()

node() {

    log.info('To use npm we will first need Node')

    log.info('Lets install the latest version of Node.')

    // Note that Node wont be downloaded and installed until we try to use it.
    Node nodeJS = new Node()

    // You can use it from the nodeJs variable
    nodeJS.npm.install('-v')

    // Or use it directly
    Npm npm = nodeJS.npm

    // Install a single package
    npm.install('chalk')

    // Install a List of packages
    npm.install(['chalk', 'debug'])

    // Install global packages the same way with installGlobal()
}
