/* groovylint-disable , FileCreateTempFile, JavaIoPackageAccess */
import jenkins.plugins.git.GitSCMSource
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever
import org.jenkinsci.plugins.workflow.libs.SCMRetriever
import hudson.plugins.filesystem_scm.FSSCM

println('== Configuring the Jenkins Pipeline library')

final String libName = 'jenkins-std-lib'
final String repoBranch = 'master'
final LibraryConfiguration lc
final File file = new File('/var/jenkins_home/pipeline-library/src')

println('==== Setting up SharedLibray')

if (file.exists()) {
    println('== Adding local lib for development')

    FSSCM scm = new FSSCM(file.getParent(), false, false, null)

    lc = new LibraryConfiguration(libName, new SCMRetriever(scm))

} else {
    println('== Adding remote lib for production use')

    String repoURL = 'https://github.com/DontShaveTheYak/jenkins-std-lib'

    println("== Using the Pipeline library from ${repoURL}")

    GitSCMSource libSource = new GitSCMSource(libName, "${repoURL}.git", null, null, null, false)

    lc = new LibraryConfiguration(libName, new SCMSourceRetriever(libSource))
}

lc.with {
    implicit = true
    defaultVersion = repoBranch
}
GlobalLibraries.get().libraries.add(lc)
println('== SharedLibray setup complete')
