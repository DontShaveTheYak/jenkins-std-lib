/* groovylint-disable, DuplicateMapLiteral, DuplicateStringLiteral, UnusedVariable */
@Library('jenkins-std-lib')

import org.dsty.logging.LogClient
import org.dsty.system.os.Path
import org.dsty.system.os.programs.CliTool
import org.dsty.js.node.Node

LogClient log = new LogClient()

node() {

    log.info('Lets install the latest version of Node.')

    Node nodeJS = new Node()

    // Note that Node wont be downloaded and installed until we try to use it.
    String version = nodeJS('-v')

    log.info("Installed latest Node version: ${version}")

    log.info('We can also specify a version of Node to install.')

    nodeJS = new Node('16.16.0')

    version = nodeJS('-v')

    log.info("Installed specific Node version: ${version}")

}
