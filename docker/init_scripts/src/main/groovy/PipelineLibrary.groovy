/* groovylint-disable , FileCreateTempFile, JavaIoPackageAccess */
import jenkins.plugins.git.GitSCMSource
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever

println('== Configuring the Jenkins Pipeline library')

final String libName = 'jenkins-std-lib'
final String repoBranch = 'master'
final LibraryConfiguration lc
final File file = new File('/var/jenkins_home/pipeline-library/src')

if (file.exists()) {
    println("===== Adding local ${libName}")

    StringBuilder stdout = new StringBuilder()
    StringBuilder stderr = new StringBuilder()

    File script = File.createTempFile('temp', '.sh')

    String lib = '/var/jenkins_home/lib'

    script.write(
        """#!/usr/bin/env bash
        mkdir ${lib} || true
        cp -r /var/jenkins_home/pipeline-library/* ${lib}

        git config --global user.email ""admin@non.existent.email""
        git config --global user.name "Jenkinsfile-runner"

        cd ${lib}
        rm -rf .git || true
        git init
        git add --all
        git commit -m init
        """
    )

    Process proc = "bash ${script.absolutePath}".execute()

    proc.consumeProcessOutput(stdout, stderr)
    proc.waitForOrKill(5000)

    if (stderr) {
        println("stdout: ${stdout}")
        println("stderr: ${stderr}")
    }

    GitSCMSource scm = new GitSCMSource(libName, lib, null, null, null, false)

    lc = new LibraryConfiguration(libName, new SCMSourceRetriever(scm))
    lc.with {
        implicit = true
        defaultVersion = repoBranch
    }
} else {
    println("===== Adding remote ${libName}")

    // TODO: change defaults once https://github.com/jenkins-infra/pipeline-library/pull/78 is merged
    String repoURL = 'https://github.com/DontShaveTheYak/jenkins-std-lib'

    println("===== Using the Pipeline library from ${repoURL}")

    GitSCMSource libSource = new GitSCMSource(libName, "${repoURL}.git", null, null, null, false)

    lc = new LibraryConfiguration(libName, new SCMSourceRetriever(libSource))
}

lc.with {
    implicit = true
    defaultVersion = repoBranch
}
GlobalLibraries.get().libraries.add(lc)
