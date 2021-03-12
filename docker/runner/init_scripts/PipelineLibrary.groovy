import hudson.plugins.filesystem_scm.FSSCM
import jenkins.plugins.git.GitSCMSource
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.jenkinsci.plugins.workflow.libs.SCMRetriever
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever

println("== Configuring the Jenkins Pipeline library")

final LibraryConfiguration lc
File file = new File("/var/jenkins_home/pipeline-library/vars")
if (!file.exists()) {
    // TODO: change defaults once https://github.com/jenkins-infra/pipeline-library/pull/78 is merged
    def defaultPipelineLibRepo = "https://github.com/oleg-nenashev/pipeline-library"
    def defaultPipelineLibBranch = "jenkinsfile-runner-support"
    println("===== Using the Pipeline library from ${defaultPipelineLibRepo} ")
    // Include https://github.com/jenkins-infra/pipeline-library
    def pipelineLibrarySource = new GitSCMSource("pipeline-library", "${defaultPipelineLibRepo}.git", null, null, null, false)
    lc = new LibraryConfiguration("pipeline-library", new SCMSourceRetriever(pipelineLibrarySource))
    lc.with {
        implicit = true
        defaultVersion = defaultPipelineLibBranch
    }
    GlobalLibraries.get().libraries.add(lc)
} else {
    println("===== Adding local Pipeline libs")

    def stdout = new StringBuilder()
    def stderr = new StringBuilder()

    File script = File.createTempFile("temp",".sh")

    def lib = "/var/jenkins_home/lib"

    script.write(
        """#!/usr/bin/env bash
        mkdir ${lib} || true
        cp -r /var/jenkins_home/pipeline-library/* ${lib}

        git config --global user.email "shadycuz@gmail.com"
        git config --global user.name "shadycuz"

        cd ${lib}
        rm -rf .git || true
        git init
        git add --all
        git commit -m init
        """
    )

    def proc = "bash ${script.absolutePath}".execute()

    proc.consumeProcessOutput(stdout, stderr)
    proc.waitForOrKill(5000)

    if (stderr) {
        println("stdout: ${stdout}")
        println("stderr: ${stderr}")
    }

    def scm = new GitSCMSource("pipeline-library", lib, null, null, null, false)

    lc = new LibraryConfiguration("pipeline-library", new SCMSourceRetriever(scm))
    lc.with {
        implicit = true
        defaultVersion = "master"
    }
}

GlobalLibraries.get().libraries.add(lc)
