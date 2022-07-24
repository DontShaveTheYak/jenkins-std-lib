package org.dsty.js.node

import org.dsty.logging.LogClient
import org.dsty.system.os.Path
import org.dsty.system.os.programs.CliTool
import org.dsty.system.os.programs.ToolBuilder
import org.dsty.system.os.shell.Result
import org.dsty.system.os.shell.ExecutionException
import org.dsty.js.node.Npm

/**
 * A class to interact with Nodejs.
 */
class Node implements Serializable {

    /**
     * Logging client
     */
    private final LogClient log

    final private NodeEnv nodeEnv = new NodeEnv()

    private CliTool nodeBin

    String version = 'latest'

    Npm npm

    Node() {
        this.log = new LogClient()
    }

    Node(String version) {
        this.log = new LogClient()
        this.version = version
    }

    private String getNodeBin() {

        if (!this.@nodeBin) {

            /* groovylint-disable-next-line DuplicateStringLiteral */
            if (this.version == 'latest') {
                this.nodeBin = this.nodeEnv.installLatest()
            } else {
                this.nodeBin = this.nodeEnv.install(this.version)            }

        }

        return this.@nodeBin
    }

    private Npm getNpm() {

        if (!this.@npm) {

            CliTool npmTool = this.createTool('npm')
            this.npm = new Npm(npmTool)

        }

        return this.@npm
    }

    /**
     * Run a command using the nodebin
     */
    Result call(String args = '') throws ExecutionException {

        return this.run(args)
    }

    /**
     * Run a command using the <strong>nodenv</strong> binary and the supplied arugments.
     *
     * @param args The arguments passed directly to <strong>nodenv</strong>.
     * @return The {@link Result} from the command execution.
     */
    private Result run(final String args) throws ExecutionException {

        return this.nodeBin.runCommand(args)

    }

    /* groovylint-disable-next-line FactoryMethodName */
    private CliTool createTool(String toolName) {

        // Calling this forces Node to be installed
        this.nodeBin

        Path shims = this.nodeEnv.nodenvInstallDir.child("./shims")

        Path toolPath = shims.child(toolName)

        if (!toolPath.exists()) {
            this.log.error("${toolName} does not exists.")

            List<String> validTools = shims.list().collect { Path tool ->
                tool.getName()
            }

            this.log.debug("Valid tools ${validTools}")
            throw new Exception("Unable to create tool ${toolName}")
        }

        return ToolBuilder.buildTool(toolPath)

    }

}
