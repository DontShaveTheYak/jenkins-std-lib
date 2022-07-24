/* groovylint-disable-next-line LineLength */
/* groovylint-disable DuplicateStringLiteral, GetterMethodCouldBeProperty, ImplicitReturnStatement, UnusedMethodParameter */
package org.dsty.js.node

import org.dsty.logging.LogClient
import org.dsty.http.Requests
import org.dsty.http.Response
import org.dsty.jenkins.Build
import org.dsty.system.System
import org.dsty.system.Platform
import org.dsty.system.os.Path
import org.dsty.system.os.shell.Shell
import org.dsty.system.os.shell.Bash
import org.dsty.system.os.shell.Result
import org.dsty.system.os.shell.ExecutionException
import org.dsty.system.os.programs.UnixExecutable
import org.dsty.system.os.programs.Executable
import org.dsty.system.os.programs.CliTool
import org.dsty.system.os.programs.ToolBuilder
import org.dsty.system.os.programs.ScriptInstaller
import org.dsty.system.os.download.HttpRetriever
import org.dsty.system.os.download.FileRetriever

/**
 * Installs and runs <a href="https://github.com/nodenv/nodenv"><strong>nodenv</strong></a>.
 * <p>
 * The <code>nodenv</code> command line tool handles the installation and switching between different
 * versions of node.
 * <pre>{@code
 * import org.dsty.js.node.NodeEnv
 *node() {
 *  NodeEnv nodenv = new NodeEnv()
 *  nodenv.installLatest() // installs the lastest node
 *&#125;}</pre>
 */
class NodeEnv implements Serializable {

    /**
     * Logging client
     */
    private final LogClient log

    /**
     * Bash Client
     */
    private final Bash bash

    /**
     * A {@link CliTool} to run commands using the <code>nodenv</code>
     * binary.
     */
    private CliTool nodeEnv

    /**
     * The version of <strong>nodenv</strong> to install.
     */
    final String version

    /**
     * The {@link Path} to the node binary.
     */
    private Path nodeBin

    /**
     * The {@link Path} to the node binary.
     */
    private Path nodenvInstallDir

    /**
    * Installs <a href="https://github.com/nodenv/nodenv"><strong>nodenv</strong></a>
    * by downloading an install script from an HTTP url.
    */
    class Installer extends ScriptInstaller {

        /**
        * The version of <code>nodenv</code> to install.
        */
        String version

        /**
        * The constructor takes a version parameter but we default to latest.
        */
        // Installer(String version = 'latest') {
        //     this.version = version
        // }

        // Installer(String version = 'latest') {
        //     this.version = version
        // }

        /**
        * This should be what we want to save the binary as.
        */
        String getBinName() {
            return 'nodenv'
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
            return ''
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

        CliTool getTool() {

            Path dummy = Path.workspace()
            Executable bin = new UnixExecutable(dummy)

            Shell shell = getShell()

            return new CliTool(shell, bin)
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
        String downloadUrl(System system) {
            return 'https://raw.githubusercontent.com/nodenv/nodenv-installer/master/bin/nodenv-installer'
        }

        /**
        * This function gets the latest version from the github release api.
        */
        // String latestVersion() {
        //     final String url = 'https://api.github.com/repos/warrensbox/node-switcher/releases/latest'

        //     final Response response = Requests.get(url, headers: ['Accept': 'application/json'])

        //     final String releaseName = response.json['tag_name']

        //     return releaseName
        // }

    }

    /**
     * Default Constructor
     * @param version Optionally specify the version of <code>node-switcher</code> to install.
     *                If not specifed the latest version is installed.
     */
    NodeEnv(final String version = 'latest') {
        this.version = version
        this.log = new LogClient()
        this.bash = new Bash()
    }

    /**
     * Run a command using the <a href="https://github.com/nodenv/nodenv"><strong>nodenv</strong></a>
     * binary.
     * <p>
     * <pre>{@code
     * import org.dsty.js.node.NodeEnv
     *node() {
     *  NodeEnv nodenv = new NodeEnv()
     *  nodenv('--help')
     *&#125;}</pre>
     * @param args The arguments passed directly to <strong>tfswtich</strong>.
     * @return The result from the command.
     */
    Result call(String args = '') throws ExecutionException {

        return this.run(args)
    }

    /**
     * Installs <code>node</code> automaticly by detecting the correct version.
     * <p>
     * The details on how to control which version of <code>node</code> to install
     * can be found in the <code>nodenv</code>
     * <a href="https://github.com/nodenv/nodenv#choosing-the-node-version">README</a>.
     *
     * @return A {@link CliTool} that can be used to run commands with the <code>node</code>
     * binary.
     */
    CliTool install() throws ExecutionException {

        try {
            this.run('install')
        } catch (ExecutionException ex) {

            if (!ex.stdErr.contains('already exists')) {
                throw ex
            }
        }

        return nodeTool()
    }

     /**
     * Installs a specific version of <code>node</code>.
     *
     * @param version The version of node that you want to install.
     * @return A {@link CliTool} that can be used to run commands with the <code>node</code>
     * binary.
     */
    CliTool install(final String version) throws ExecutionException {

        // If the requested version is already installed then it fails =/
        // So we need to do some error checking here

        try {
            this.run("install ${version}")
            this.run("global ${version}")
        } catch (ExecutionException ex) {

            if (!ex.stdErr.contains('already exists')) {
                throw ex
            }
        }

        return nodeTool()
    }

    /**
     * Installs a the latest version of <code>node</code>.
     *
     * @return A {@link CliTool} that can be used to run commands with the <code>node</code>
     * binary.
     */
    CliTool installLatest() throws ExecutionException {

        // Need to programaticly get the lastest somehow

        final String version = '18.5.0'

        try {
            this.run("install ${version}")
            this.run("global ${version}")
        } catch (ExecutionException ex) {

            if (!ex.stdErr.contains('already exists')) {
                throw ex
            }
        }

        return nodeTool()
    }

    /**
    * Install <code>nodenv</code> if it is not already installed.
    *
    * @return A {@link CliTool} to run commands using <code>nodenv</code>.
    */
    private CliTool getNodeEnv() {

        if (!this.@nodeEnv) {

            // The installer doesn't allow us to set a path... so it actually ends up in the home directory.
            Path binPath = this.nodenvInstallDir.child('bin')
            Path shimPath = this.nodenvInstallDir.child('shims')

            // Add the install directory to PATH
            final Build build = new Build()
            Object wfs = build.getWorkFlowScript()

            String newPath = "${shimPath}:${binPath}:${wfs.env.PATH}"

            wfs.env.PATH = newPath

            // Install nodenv
            final Installer installer = new Installer()
            installer.install()

            String binName = installer.getBinName()

            this.nodeEnv = ToolBuilder.buildTool(binPath.child(binName))
        }

        return this.@nodeEnv
    }

    private String getNodenvInstallDir() {

        if (!this.@nodenvInstallDir) {

            final Path installPath = Path.jenkinsHome().child('./.nodenv')

            this.nodenvInstallDir = installPath
        }

        return this.@nodenvInstallDir
    }

    /**
    * Get the {@link Path} to the node binary.
    * <p>
    * This will create the cache directory <code>.tools/node</code>,
    * if it doesn't exist.
    *
    * @return The {@link Path} to the node binary.
    */
    private String getNodeBin() {

        if (!this.@nodeBin) {

            final Path nodePath = this.nodenvInstallDir.child('./shims/node')

            this.nodeBin = nodePath
        }

        return this.@nodeBin
    }

    private CliTool nodeTool() {
        return ToolBuilder.buildTool(this.nodeBin)
    }

    /**
     * Run a command using the <strong>nodenv</strong> binary and the supplied arugments.
     *
     * @param args The arguments passed directly to <strong>nodenv</strong>.
     * @return The {@link Result} from the command execution.
     */
    private Result run(final String args) throws ExecutionException {

        return this.nodeEnv.runCommand(args)

    }

}
