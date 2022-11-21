/* groovylint-disable, DuplicateMapLiteral, DuplicateStringLiteral, UnusedVariable */
@Library('jenkins-std-lib')

import org.dsty.logging.LogClient
import org.dsty.system.os.Path
import org.dsty.system.os.programs.CliTool
import org.dsty.js.node.Node
import org.dsty.js.node.Npm
import org.dsty.vscode.DevContainer
import org.dsty.scm.Git

LogClient log = new LogClient()

node() {

    log.info('To use vscode devcontainers we will first need Node and npm.')

    log.info('Lets install the latest version of Node and npm.')

    // Note that Node wont be downloaded and installed until we try to use it.
    Node nodeJS = new Node()

    DevContainer devcontainer = new DevContainer(nodeJS.npm)

    log.info('Lets clone the rust example project https://github.com/microsoft/vscode-remote-try-rust')

    Git git = new Git(this)

    git.checkout(
        changelog: false,
        poll: false,
        scm: [
            $class: 'GitSCM',
            branches: [[name: 'main']],
            extensions: [],
            userRemoteConfigs: [[url: 'https://github.com/microsoft/vscode-remote-try-rust.git']]
        ]
    )

    log.info('Now we can start the devcontainer and run a command inside it.')

    devcontainer.withContainer {
        devcontainer.exec("cargo run")
    }

    // The devcontainer will be stopped but not removed.

}
