#!groovy
/* groovylint-disable GStringExpressionWithinString, JavaIoPackageAccess, NoDef, NoWildcardImports, UnnecessaryGetter, UnnecessaryObjectReferences, UnnecessarySetter, VariableTypeRequired */
import hudson.model.FreeStyleProject
import hudson.triggers.SCMTrigger
import javaposse.jobdsl.plugin.*
import jenkins.model.Jenkins
import hudson.plugins.filesystem_scm.FSSCM

println('==== Creating seed job')
final String jobName = 'seed-job'
final File jobsDir = new File('/var/jenkins_home/seed-jobs')

final String dslScript = '''\
import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.Paths


Files.walk(Paths.get('/var/jenkins_home/seed-jobs')).findAll { Path item ->

    final String jobName = item.toString()
    jobName.contains('.groovy') && jobName.contains('example')

}.forEach { Path item ->
    final String fileName = item.getFileName()
    final String jobName = fileName.replace('_', '-').replace('.groovy', '')
    final File jenkinsFile = item.toFile()
    println("Creating job for ${fileName}")

      pipelineJob(jobName) {

      definition {
        cpsScm {

            lightweight(true)

            scm {
                filesystem {

                    // The file path for the source code.
                    path(jenkinsFile.getParent())
                    // If true, the system will delete all existing files/sub-folders in workspace before checking-out.
                    clearWorkspace(false)
                    // If true, the system will copy hidden files and folders as well.
                    copyHidden(false)
                    filterSettings {
                         includeFilter(false)
                    }

                }
            }

            scriptPath(fileName)

        }
      }
    }
}
'''

final FSSCM fileScm = new FSSCM(jobsDir.getPath(), false, false, false, false, null)

final Jenkins jenkins = Jenkins.getInstance()

def existingJob = jenkins.items.find { def job ->
    job.name == jobName
}

if (existingJob) {
    println('== Existing seed job found, exiting')
    return
}

final SCMTrigger scmTrigger = new SCMTrigger('* * * * *')
final ExecuteDslScripts dslBuilder = new ExecuteDslScripts()

// dslBuilder.setSandbox(true)
dslBuilder.setScriptText(dslScript)
dslBuilder.setUseScriptText(true)
dslBuilder.setIgnoreExisting(false)
dslBuilder.setIgnoreMissingFiles(false)
dslBuilder.setFailOnMissingPlugin(true)
dslBuilder.setRemovedJobAction(RemovedJobAction.DELETE)
dslBuilder.setRemovedViewAction(RemovedViewAction.DELETE)
dslBuilder.setLookupStrategy(LookupStrategy.JENKINS_ROOT)

dslProject = new FreeStyleProject(jenkins, jobName)
dslProject.scm = fileScm


dslProject.addTrigger(scmTrigger)
dslProject.createTransientActions()
dslProject.getPublishersList().add(dslBuilder)

println('== Adding Seed Job to Jenkins')
jenkins.add(dslProject, jobName)

println('== Triggering Seed Job polling')
scmTrigger.start(dslProject, true)

println('== Seed Job setup complete')
