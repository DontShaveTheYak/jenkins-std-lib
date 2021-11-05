/* groovylint-disable DuplicateStringLiteral */
package org.dsty.github.actions

import org.dsty.bash.BashClient
import org.dsty.bash.Result
import org.dsty.bash.ScriptError
import org.dsty.logging.LogClient

/**
 * Runs Github Actions workflows using the <a href="https://github.com/nektos/act"><strong>act</strong></a> CLI tool.
 * <p>To run a Github workflow in a jenkins ScriptedPipeline:
 * <pre>{@code
 * import org.dsty.github.actions.Workflow
 *node() {
 *  Workflow workflow = new Workflow(this)
 *  workflow()
 *&#125;}</pre>
 * <p> For more details see {@link #call(java.lang.String) call}.
 */
class Workflow implements Serializable {

    /**
     * Workflow script representing the jenkins build.
     */
    private final Object steps

    /**
     * Logging client
     */
    private final LogClient log

    /**
     * Bash Client
     */
    private final BashClient bash

    /**
     * The version of <strong>act</strong> to install.
     */
    String version

    /**
     * Default Constructor
     * @param steps The workflow script representing the jenkins build.
     */
    Workflow(Object steps) {
        this.steps = steps
        this.log = new LogClient(steps)
        this.bash = new BashClient(steps)
    }

    /**
     * Installs and then runs <strong>act</strong>.
     * <p> The default event is <strong>push</strong> to change it to <strong>pull_request</strong>:</p>
     * <pre>{@code
     * import org.dsty.github.actions.Workflow
     *node() {
     *  Workflow workflow = new Workflow(this)
     *  workflow('pull_request')
     *&#125;}</pre>
     * @param args The arguments passed directly to <strong>act</strong>.
     * See <strong>act</strong> <a href="https://github.com/nektos/act#commands">docs</a>.
     * @return The output from the <strong>act</strong> command.
     */
    String call(String args = '') throws ScriptError {
        this.install()

        return this.run(args)
    }

    /**
     * Installs <strong>act</strong> by first checking if it is already installed and
     * if not then installs the latest version. The property {@link #version} can be set to install
     * a specific version.
     */
    void install() throws ScriptError {

        final String installerURL = 'https://raw.githubusercontent.com/nektos/act/master/install.sh'

        final String versionCmd = "set -o pipefail; ${this.steps.env.WORKSPACE}/act --version | cut -d' ' -f3"

        this.log.info('Installing Act.')

        Result result = this.bash.ignoreErrors(versionCmd, false, true)

        Closure alreadyInstalled = {
            this.version = "v${result.stdOut}"

            this.log.info("Version ${this.version} of act is already installed.")
        }

        Closure installLatest = {
            this.log.info('Downloading the latest version.')
            this.bash.call("curl ${installerURL} | bash -s -- -b ${this.steps.env.WORKSPACE}")
        }

        Closure installVersion = {
            this.log.info("Downloading version ${this.version}.")
            this.bash.call("""\
                curl ${installerURL} -o .install_act
                chmod +x .install_act
                bash .install_act -b ${this.steps.env.WORKSPACE} ${this.version}""".stripIndent()
            )

            this.version = null
        }

        Closure installCMD = this.version ? installVersion : installLatest

        Closure cmd = result.exitCode == 0 && !this.version ? alreadyInstalled : installCMD

        this.steps.dir(this.steps.env.WORKSPACE) {
            cmd()
        }

        if (!this.version) {
            String version = this.bash.call(versionCmd).stdOut
            this.version = "v${version}"
        }

        String homeDir = this.bash.call('echo ~').stdOut

        if (!this.steps.fileExists("${homeDir}/.actrc")) {
            // We have to write out a config file to make act work headless
            this.bash.call('''\
                cat <<EOT >> ~/.actrc
                -P ubuntu-latest=catthehacker/ubuntu:act-latest
                -P ubuntu-20.04=catthehacker/ubuntu:act-20.04
                -P ubuntu-18.04=catthehacker/ubuntu:act-18.04
                EOT'''.stripIndent()
            )
        }
    }

    /**
     * Calls the <strong>act</strong> binary with the supplied arugments.
     * @param args The arguments passed directly to <strong>act</strong>.
     * See <strong>act</strong> <a href="https://github.com/nektos/act#commands">docs</a>.
     * @return The output from the <strong>act</strong> command.
     */
    String run(String args) throws ScriptError {
        Result result = this.bash.call("${this.steps.env.WORKSPACE}/act ${args}")

        return result.output
    }

}
