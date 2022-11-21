package org.dsty.vscode

import org.dsty.jenkins.Build
import org.dsty.logging.LogClient
import org.dsty.system.os.Path
import org.dsty.system.os.programs.CliTool
import org.dsty.system.os.programs.ToolBuilder
import org.dsty.system.os.shell.Bash
import org.dsty.system.os.shell.Result
import org.dsty.system.os.shell.ExecutionException
import org.dsty.js.node.Npm

import com.cloudbees.groovy.cps.NonCPS

import java.util.regex.Matcher

/**
 * Installs and runs the vscode <a href="https://github.com/devcontainers/cli"target="_blank">
 * <strong>devcontainer</strong></a> CLI.
 * <p>
 * The devcontainer tool can be used to create and manage devcontainers created from a
 * <strong>devcontainer.json</strong>.
 * <pre>{@code
 * import org.dsty.vscode.DevContainer
 *node() {
 *  DevContainer devcontainer = new DevContainer()
 *  devcontainer.withContainer {
 *    devcontainer.exec('echo "Hello from inside DevContainer"')
 *  &#125;
 *&#125;}</pre>
 */
class DevContainer implements Serializable {

    /**
     * Logging client
     */
    private final LogClient log

    private CliTool devContainer

    private Npm npm

    private String workspace

    private Bash bash

    DevContainer(Npm npm) {
        this.npm = npm
        this.log = new LogClient()
        this.bash = new Bash()
    }

    private Npm getDevContainer() {

        if (!this.@devContainer) {

            this.npm.installGlobal('@devcontainers/cli')

            String prefix = this.npm.call('get prefix').stdOut.trim()
            Path devContainerPath = new Path(prefix).child('bin/devcontainer')

            this.devContainer = ToolBuilder.buildTool(devContainerPath)

        }

        return this.@devContainer
    }

    private String getWorkspace() {

        return Path.cwd()
    }

    /**
     * Run a command using the <strong>devcontainer</strong></a> executable.
     *
     * @param args The arguments passed directly to <strong>devcontainer</strong>.
     * @return The result from the command.
     */
    Result call(String args = '') throws ExecutionException {

        return this.run(args)
    }

    /**
     * Calls the devcontainer CLI's <strong>up</strong> command to start the devcontainer
     * in <strong>devcontainer.json</strong>.
     * <p>
     * This command will not stop the container for you. If you want to have the container
     * stopped automaticly see {@link #withContainer()}.
     *
     * @param workspace A path to a directory from where all commands will be executed.
     *                  If a path is not specified the current directory is used.
     * @return The result from the command.
     */
    Result up(Path workspace = null) {

        if (workspace == null) {
            workspace = this.workspace
        }

        this.run("up --workspace-folder ${workspace}")
    }

    /**
     * Calls the devcontainer CLI's <strong>exec</strong> command to run a command inside the devcontainer.
     *
     * @param cmd The command to run inside the container.
     * @param workspace A path to a directory from where all commands will be executed.
     *                  If a path is not specified the current directory is used.
     * @return The result from the command.
     */
    Result exec(String cmd, Path workspace = null) {

        if (workspace == null) {
            workspace = this.workspace
        }

        this.run("exec --workspace-folder ${workspace} ${cmd}")
    }

    /**
     * Bring up a devcontainer, runs the user supplied code and then stops the container.
     * <p>
     * Similar to {@link #up()}, but stops the container automaticlly.
     *
     * @param workspace A path to a directory from where all commands will be executed.
     *                  If a path is not specified the current directory is used.
     * @param userCode The code to be ran after the devcontainer has been started.
     */
    void withContainer(String workspace = null, Closure userCode) {

        String output = this.up(workspace)

        String containerId = this.parseContainerId(output)

        log.debug("Started devcontainer ${containerId}")

        try {
            userCode()
        } finally {
            this.bash.silent("docker stop ${containerId}")
        }

    }

    /**
     * Run a command using the <strong>devcontainer</strong> executable using the supplied arugments.
     *
     * @param args The arguments passed directly to <strong>devcontainer</strong> executable.
     * @return The {@link Result} from the command execution.
     */
    private Result run(final String args) throws ExecutionException {

        return this.devContainer.runCommand(args)

    }

    /**
     * Parse the Docker container id from the output of devcontainer up.
     * @param input to search for container id.
     * @returns the container id if found.
     */
    @NonCPS
    private String parseContainerId(String input) {

        Matcher matcher = input =~ /(?:\"containerId\")(?:\:\")(\w*)(?:\")/

        if (!matcher.find()) {
            throw new Exception("Unable to find a container ID in ${input}")
        }

        String containerID = matcher[0][1]

        return containerID
    }
}
