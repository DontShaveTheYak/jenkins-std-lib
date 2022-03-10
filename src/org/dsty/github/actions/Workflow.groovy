/* groovylint-disable DuplicateStringLiteral, GetterMethodCouldBeProperty */
package org.dsty.github.actions

import org.dsty.logging.LogClient
import org.dsty.http.Requests
import org.dsty.http.Response
import org.dsty.system.System
import org.dsty.system.Platform
import org.dsty.system.os.Path
import org.dsty.system.os.shell.Shell
import org.dsty.system.os.shell.Bash
import org.dsty.system.os.shell.Result
import org.dsty.system.os.shell.ExecutionException
import org.dsty.system.os.programs.CliTool
import org.dsty.system.os.programs.ScriptInstaller
import org.dsty.system.os.download.HttpRetriever
import org.dsty.system.os.download.FileRetriever

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
    private final Bash bash

    private CliTool act

    /**
     * The version of <strong>act</strong> to install.
     */
    String version

    /**
    * Installs Act by downloading an install script from an HTTP url.
    */
    class ActInstaller extends ScriptInstaller {

        /**
        * The version of act to install.
        */
        String version

        /**
        * The constructor takes a version parameter but we default to latest.
        */
        ActInstaller(String version = 'latest') {
            this.version = version
        }

        /**
        * This should be what we want to save the binary as.
        */
        String getBinName() {
            return 'act'
        }

        /**
        * This is only needed if our binary is in a subdirectory of
        * a tar/zip. Since we are downloading the binary directly,
        * we can just use the binary name.
        */
        String getBinPath() {
            /* groovylint-disable-next-line UnnecessaryGetter */
            return getBinName()
        }

        /**
        * This is the arguments to pass to the installer. The installer
        * must allow you to set the path where the binary is installed and
        * you must use installDir() for everything to work.
        */
        String getScriptArgs() {
            return "-b ${installDir()} ${getVersion()}"
        }

        /**
        * You can set the shell that you want to use when calling
        * the install script. Here we are using the default shell,
        * which is bash on unix systems.
        */
        Shell getShell() {
            /* groovylint-disable-next-line UnnecessaryGetter */
            return Platform.getShell()
        }

        /**
        * Set to true if the downloaded file will be a tar/zip.
        */
        Boolean isArchive() {
            return false
        }

        /**
        * How we want to retrieve the file. Currently only dirrectly via
        * the HttpRetriever or via a tar/zip retrieved via HTTP with the ArchiveRetriever.
        * <p>
        * In the future I would like to support other retrievers like pip, npm and AWS S3.
        */
        Class<FileRetriever> getRetriever() {
            return HttpRetriever
        }

        /**
        * This function is needed to dynamicly change the version
        * from latest to an actual version string.
        */
        String getVersion() {

            if (this.@version == 'latest') {
                this.version = latestVersion()
            }

            return this.@version
        }

        /**
        * This sets the correct download url.
        * <p>
        * The System object is passed in which can provide the type of system
        * and information about the hardware.
        */
        String downloadUrl(System system) { // groovylint-disable-line
            return 'https://raw.githubusercontent.com/nektos/act/master/install.sh'
        }

        /**
        * This function gets the latest version from the github release api.
        */
        String latestVersion() {
            final String url = 'https://api.github.com/repos/nektos/act/releases/latest'

            final Response response = Requests.get(url, headers: ['Accept': 'application/json'])

            final String releaseName = response.json['tag_name']

            return releaseName
        }

    }

    /**
     * Default Constructor
     * @param steps The workflow script representing the jenkins build.
     */
    Workflow(Object steps) {
        this.steps = steps
        this.log = new LogClient(steps)
        this.bash = new Bash()
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
    String call(String args = '') throws ExecutionException {

        return this.run(args)
    }

    /**
     * Installs <strong>act</strong> by first checking if it is already installed and
     * if not then installs the latest version. The property {@link #version} can be set to install
     * a specific version.
     */
    CliTool install() throws ExecutionException {

        final String version = this.version ?: 'latest'

        final ActInstaller installer = new ActInstaller(version)

        final CliTool act = installer.install()

        final Path homeDir = Path.userHome()

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

        return act
    }

    /**
     * Calls the <strong>act</strong> binary with the supplied arugments.
     * @param args The arguments passed directly to <strong>act</strong>.
     * See <strong>act</strong> <a href="https://github.com/nektos/act#commands">docs</a>.
     * @return The output from the <strong>act</strong> command.
     */
    String run(String args) throws ExecutionException {

        this.act = this.act ?: this.install()

        Result result = this.act.runCommand(args)

        return result.output
    }

}
