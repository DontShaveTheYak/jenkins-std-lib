/* groovylint-disable DuplicateStringLiteral */
package org.dsty.github.actions

import com.cloudbees.groovy.cps.NonCPS

/**
* Github Action that uses JavaScript.
*/
class JavaScriptAction extends DockerAction implements GithubAction {

    /**
     * Default Constructor
     * <p>Using this Class directly in a Jenkins pipeline is an Advanced
     * use case. Most people should just use {@link org.dsty.github.actions.Step#Step Step}.
     * @param steps The workflow script representing the jenkins build.
     */
    JavaScriptAction(Object steps) {
        super(steps)
    }

    /**
     * This method will prepare the Action, run the action and then parse the output.
     * @returns the outputs from the action.
     */
    @Override
    Map run() {
        String actionDir = '/github/action'
        String workspace = "${this.options.workspace}/${this.name}"

        this.metadata.runs.entrypoint = 'node'
        this.metadata.runs.args = [
            "${actionDir}/${this.metadata.runs.main}"
        ]
        this.metadata.runs['pre-entrypoint'] = this.metadata.runs.pre ?: ''
        this.metadata.runs['post-entrypoint'] = this.metadata.runs.post ?: ''

        this.mounts.add("${workspace}:${actionDir}")

        this.log.info(this.log.pprint(this.options))
        this.log.info(this.log.pprint(this.metadata))

        String nodeVersion = this.metadata.runs.using

        Number version = nodeVersion.find( /\d+/ ).toInteger()

        Map outputs = this.actionRun("node:${version}")

        return outputs
    }

}
