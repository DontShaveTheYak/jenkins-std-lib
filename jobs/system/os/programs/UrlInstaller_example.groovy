// groovylint-disable-next-line
/* groovylint-disable DuplicateMapLiteral, DuplicateStringLiteral, GetterMethodCouldBeProperty, ThrowException, UnnecessaryGetter, UnnecessarySubstring */
@Library('jenkins-std-lib')
import org.dsty.http.Requests
import org.dsty.http.Response
import org.dsty.system.System
import org.dsty.system.os.Path
import org.dsty.system.os.programs.CliTool
import org.dsty.system.os.programs.UrlInstaller
import org.dsty.system.os.download.FileRetriever
import org.dsty.system.os.download.ArchiveRetriever

/**
 * Installs Terraform by downloading the zip file from a HTTP url and
 * then unpacking it.
 */
class TerraformInstaller extends UrlInstaller {

    /**
     * The version of terraform to install.
     */
    String version

    /**
     * The constructor takes a version parameter but we default to latest.
     */
    TerraformInstaller(String version = 'latest') {
        this.version = version
    }

    /**
     * This should be what we want to save the binary as.
     */
    String getBinName() {
        return 'terraform'
    }

    /**
     * This is only needed if our binary is in a subdirectory of
     * a tar/zip. Since the binary is in the root of the zip file,
     * we can just use the binary name.
     */
    String getBinPath() {
        return getBinName()
    }

    /**
     * How we want to retrieve the file. Currently only dirrectly via
     * the HttpRetriever or via a tar/zip retrieved via HTTP with the ArchiveRetriever.
     * <p>
     * In the future I would like to support other retrievers like pip, npm and AWS S3.
     */
    Class<FileRetriever> getRetriever() {
        return ArchiveRetriever
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
     * Set to true if the downloaded file will be a tar/zip.
     */
    Boolean isArchive() {
        return true
    }

    /**
     * This sets the correct download url.
     * <p>
     * The System object is passed in which can provide the type of system
     * and information about the hardware.
     */
    String downloadUrl(System system) {

        if (system.name() != 'UNIX') {
            throw new Exception('Unsupported System.')
        }

        final String systemName = 'linux'
        final String arch = system.architecture()

        final String baseUrl = 'https://releases.hashicorp.com'

        // https://releases.hashicorp.com/terraform/1.0.7/terraform_1.0.7_linux_amd64.zip
        return "${baseUrl}/${getBinName()}/${getVersion()}/${getBinName()}_${getVersion()}_${systemName}_${arch}.zip"
    }

    /**
     * This function gets the latest version from the github release api.
     */
    String latestVersion() {
        final String url = 'https://api.github.com/repos/hashicorp/terraform/releases/latest'

        final Response response = Requests.get(url, headers: ['Accept': 'application/json'] )

        final String releaseName = response.json['tag_name']

        // remove the v from the tag_name
        return releaseName.substring(1)
    }

}

node() {

    // Ignore the next line its for catching CPS issues.
    sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true) // groovylint-disable-line

    // Clear the workspace
    Path.workspace().deleteContents()

    // The version we want to install
    final String pinnedVersion = '1.0.0'

    TerraformInstaller tfInstaller = new TerraformInstaller(pinnedVersion)

    // Install the terraform binary and get back a generic cli tool
    CliTool terraform = tfInstaller.install()

    // Get the version output for testing
    String version = terraform.runCommand('--version').stdOut

    // Test that the version is the one we pinned
    if (!version.contains(pinnedVersion)) {

        error("The version ${pinnedVersion} was not installed.")

    }

    // Does nothing since it is already installed
    tfInstaller.install()

    // Install the latest version
    tfInstaller = new TerraformInstaller()

    terraform = tfInstaller.install()

    version = terraform.runCommand('--version').stdOut

    // Test the version installed is no longer the pinned version
    if (version.contains(pinnedVersion)) {

        error('The latest version was not installed.')

    }

    // Ignore the next line its for catching CPS issues.
    sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

}
