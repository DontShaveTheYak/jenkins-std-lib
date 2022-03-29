/* groovylint-disable-next-line LineLength */
/* groovylint-disable DuplicateStringLiteral, GetterMethodCouldBeProperty, ImplicitReturnStatement, UnusedMethodParameter */
package org.dsty.iac.terraform

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
import org.dsty.system.os.programs.ToolBuilder
import org.dsty.system.os.programs.ScriptInstaller
import org.dsty.system.os.download.HttpRetriever
import org.dsty.system.os.download.FileRetriever

/**
 * Installs and runs <a href="https://github.com/warrensbox/terraform-switcher"><strong>tfswitch</strong></a>.
 * <p>
 * The <code>tfswitch</code> command line tool handles the installation and switching between different
 * versions of terraform.
 * <pre>{@code
 * import org.dsty.iac.terraform.TerraformSwitcher
 *node() {
 *  TerraformSwitcher tfswitch = new TerraformSwitcher()
 *  tfswitch.installLatest() // installs the lastest terraform
 *&#125;}</pre>
 */
class TerraformSwitcher implements Serializable {

    /**
     * Logging client
     */
    private final LogClient log

    /**
     * Bash Client
     */
    private final Bash bash

    /**
     * A {@link CliTool} to run commands using the <code>tfswitch</code>
     * binary.
     */
    private CliTool tfswitch

    /**
     * The version of <strong>tfswitch</strong> to install.
     */
    final String version

    /**
     * The {@link Path} to the terraform binary.
     */
    private Path tfBin

    /**
    * Installs <a href="https://github.com/warrensbox/terraform-switcher"><strong>tfswitch</strong></a>
    * by downloading an install script from an HTTP url.
    */
    class Installer extends ScriptInstaller {

        /**
        * The version of <code>terraform-switcher</code> to install.
        */
        String version

        /**
        * The constructor takes a version parameter but we default to latest.
        */
        Installer(String version = 'latest') {
            this.version = version
        }

        /**
        * This should be what we want to save the binary as.
        */
        String getBinName() {
            return 'tfswitch'
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
        String downloadUrl(System system) {
            return 'https://raw.githubusercontent.com/warrensbox/terraform-switcher/master/install.sh'
        }

        /**
        * This function gets the latest version from the github release api.
        */
        String latestVersion() {
            final String url = 'https://api.github.com/repos/warrensbox/terraform-switcher/releases/latest'

            final Response response = Requests.get(url, headers: ['Accept': 'application/json'])

            final String releaseName = response.json['tag_name']

            return releaseName
        }

    }

    /**
     * Default Constructor
     * @param version Optionally specify the version of <code>terraform-switcher</code> to install.
     *                If not specifed the latest version is installed.
     */
    TerraformSwitcher(final String version = 'latest') {
        this.version = version
        this.log = new LogClient()
        this.bash = new Bash()
    }

    /**
     * Run a command using the <a href="https://github.com/warrensbox/terraform-switcher"><strong>tfswitch</strong></a>
     * binary.
     * <p>
     * <pre>{@code
     * import org.dsty.iac.terraform.TerraformSwitcher
     *node() {
     *  TerraformSwitcher tfswitch = new TerraformSwitcher()
     *  tfswitch('--help')
     *&#125;}</pre>
     * @param args The arguments passed directly to <strong>tfswtich</strong>.
     * @return The result from the command.
     */
    Result call(String args = '') throws ExecutionException {

        return this.run(args)
    }

    /**
     * Installs terraform by automaticly detecting the correct version.
     * <p>
     * The details on how to control which version of terraform to install
     * can be found in the <code>tfswitch</code> <a href="https://github.com/warrensbox/terraform-switcher">README</a>.
     *
     * @return A {@link CliTool} that can be used to run commands with the <code>terraform</code>
     * binary.
     */
    CliTool install() throws ExecutionException {

        this.run("-b ${this.tfBin}")

        return terraformTool()
    }

     /**
     * Installs a specific version of <code>terraform</code>.
     *
     * @param version The version of terraform that you want to install.
     * @return A {@link CliTool} that can be used to run commands with the <code>terraform</code>
     * binary.
     */
    CliTool install(final String version) throws ExecutionException {

        this.run("-b ${this.tfBin} ${version}")

        return terraformTool()
    }

    /**
     * Installs a the latest version of <code>terraform</code>.
     *
     * @return A {@link CliTool} that can be used to run commands with the <code>terraform</code>
     * binary.
     */
    CliTool installLatest() throws ExecutionException {

        this.run("-b ${this.tfBin} --latest")

        return terraformTool()
    }

    /**
    * Install <code>tfswitch</code> if it is not already installed.
    *
    * @return A {@link CliTool} to run commands using <code>tfswitch</code>.
    */
    private CliTool getTfswitch() {

        if (!this.@tfswitch) {
            final Installer installer = new Installer(this.version)
            this.tfswitch = installer.install()
        }

        return this.@tfswitch
    }

    /**
    * Get the {@link Path} to the terraform binary.
    * <p>
    * This will create the cache directory <code>.tools/terraform</code>,
    * if it doesn't exist.
    *
    * @return The {@link Path} to the terraform binary.
    */
    private String getTfBin() {

        if (!this.@tfBin) {

            final Path homeDir = Path.workspace()

            Path tfDir = homeDir.child('.tools/terraform')
            if (!tfDir.exists()) {
                tfDir.mkdirs()
            }

            this.tfBin = tfDir.child('terraform')
        }

        return this.@tfBin
    }

    private CliTool terraformTool() {
        return ToolBuilder.buildTool(this.tfBin)
    }

    /**
     * Run a command using the <strong>tfswitch</strong> binary and the supplied arugments.
     *
     * @param args The arguments passed directly to <strong>tfswitch</strong>.
     * @return The {@link Result} from the command execution.
     */
    private Result run(final String args) throws ExecutionException {

        return this.tfswitch.runCommand(args)

    }

}
