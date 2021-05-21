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

    if (this.steps.env.DIND_JENKINS_HOME) {

      List parts = this.options.workspace.split('/workspace/')

      workspace = "${this.steps.env.DIND_JENKINS_HOME}/workspace/${parts[1]}/${this.name}"

    }

    this.metadata.runs.entrypoint = 'node'
    this.metadata.runs.args = ["${actionDir}/${this.metadata.runs.main}"
    ]
    this.metadata.runs['pre-entrypoint'] = this.metadata.runs.pre ?: ''
    this.metadata.runs['post-entrypoint'] = this.metadata.runs.post ?: ''

    this.mounts.add("${workspace}:${actionDir}")

    this.log.info(this.log.pprint(this.options))
    this.log.info(this.log.pprint(this.metadata))

    Map outputs = this.actionRun('node:12.20.1-buster-slim')

    return outputs

  }

  /**
   * Parses the input for Github Actions outputs.
   * @param input to search for Github Actions outputs.
   * @returns the outputs found.
   */
  @NonCPS
  @Override
  Map parseOutputs(String input) {

    Map outputs = [:]

    List matches = (input =~ /(?m)##\[set-output.*$/).findAll()

    for (match in matches) {
      String outputName = (match =~ /(?m)(?<=name=).*(?=;])/).findAll().first()
      String outputValue = (match =~ /(?m);](.*$)/).findAll().first()[1]

      outputs[outputName] = outputValue
    }

    return outputs

  }

  /**
   * Removes the Github Actions outputs so it can be displayed
   * to the user.
   * @param input to search for Github Actions outputs.
   * @returns the input free of any Github Action outputs.
   */
  @NonCPS
  @Override
  String cleanOutput(String input) {

    return (input =~ /(?m)##\[set-output.*$/).replaceAll('')

  }

}
