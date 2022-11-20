/* groovylint-disable, DuplicateMapLiteral, DuplicateStringLiteral, UnusedVariable */
@Library('jenkins-std-lib')

import org.dsty.logging.LogClient
import org.dsty.system.os.Path
import org.dsty.system.os.programs.CliTool
import org.dsty.js.node.NodeEnv

LogClient log = new LogClient()

node() {

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

    String nodeVersion = '''\
17.9.1
'''

    log.info('You can also specify the version in the .node-version file')

    log.info("Lets create a .node-version file with version ${nodeVersion}")

    Path ws = Path.workspace()

    Path nodeVersionFile = ws.child('.node-version')

    ws.withTempFile(nodeVersionFile) {

        nodeVersionFile.write(nodeVersion)

        sh("cat ${nodeVersionFile}")

        node = nodenv.install()

        node.runCommand('-v')

    }

}
