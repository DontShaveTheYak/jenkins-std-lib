package org.dsty.js.node

import org.dsty.logging.LogClient
import org.dsty.system.os.Path
import org.dsty.system.os.programs.CliTool
import org.dsty.system.os.programs.ToolBuilder
import org.dsty.system.os.shell.Result
import org.dsty.system.os.shell.ExecutionException

/**
 * A class to interact with Npm.
 */
class Npm implements Serializable {

    /**
     * Logging client
     */
    private final LogClient log

    private final CliTool npm

    Npm(CliTool npm) {
        this.log = new LogClient()
        this.npm = npm
    }

    /**
     * Run a command using the <strong>npm</strong></a> binary.
     *
     * @param args The arguments passed directly to <strong>npm</strong>.
     * @return The result from the command.
     */
    Result call(String args = '') throws ExecutionException {

        return this.run(args)
    }

    void install(String packageName) {

        this.run("install ${packageName}")
    }

    void install(List<String> packages) {

        String packagesArg = packages.join(' ')

        this.run("install ${packagesArg}")
    }

    void installGlobal(String packageName) {

        this.run("install -g ${packageName}")
    }

    void installGlobal(List<String> packages) {

        String packagesArg = packages.join(' ')

        this.run("install -g ${packagesArg}")
    }

    /**
     * Run a command using the <strong>npm</strong> binary and the supplied arugments.
     *
     * @param args The arguments passed directly to <strong>nodenv</strong>.
     * @return The {@link Result} from the command execution.
     */
    private Result run(final String args) throws ExecutionException {

        return this.npm.runCommand(args)

    }
}
