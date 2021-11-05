// groovylint-disable-next-line
/* groovylint-disable DuplicateMapLiteral, DuplicateStringLiteral, GetterMethodCouldBeProperty, ThrowException, UnnecessaryGetter, UnnecessarySubstring */
@Library('jenkins-std-lib')
import org.dsty.http.Requests
import org.dsty.http.Response
import org.dsty.system.System
import org.dsty.system.Platform
import org.dsty.system.os.Path
import org.dsty.system.os.shell.Shell
import org.dsty.system.os.programs.CliTool
import org.dsty.system.os.programs.ScriptInstaller
import org.dsty.system.os.download.HttpRetriever
import org.dsty.system.os.download.FileRetriever

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

        final Response response = Requests.get(url, headers: ['Accept': 'application/json'] )

        final String releaseName = response.json['tag_name']

        return releaseName
    }

}

node() {

    // Ignore the next line its for catching CPS issues.
    sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    // Clear the workspace
    Path.workspace().deleteContents()

    // The version we want to install
    final String pinnedVersion = '0.2.22'

    ActInstaller actInstaller = new ActInstaller("v${pinnedVersion}")

    // Install the act binary and get back a generic cli tool
    CliTool act = actInstaller.install()

    // Get the version output for testing
    String version = act.runCommand('--version').stdOut

    // Test that the version is the one we pinned
    if (!version.contains(pinnedVersion)) {

        error("The version ${pinnedVersion} was not installed.")

    }

    // Does nothing since it is already installed
    actInstaller.install()

    // Install the latest version
    actInstaller = new ActInstaller()

    act = actInstaller.install()

    version = act.runCommand('--version').stdOut

    // Test the version installed is no longer the pinned version
    if (version.contains(pinnedVersion)) {

        error('The latest version was not installed.')

    }

    // Ignore the next line its for catching CPS issues.
    sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

}
