package org.dsty.github

import org.dsty.logging.LogClient

/**
* Determine the type of Action being used a returns the
* proper class to represent that action.
*/
class ActionFactory implements Serializable {

  /**
   * Workflow script representing the jenkins build.
   */
  Object steps

  /**
   * Logging client
   */
  LogClient log

  /**
   * Default Constructor
   * @param steps The workflow script representing the jenkins build.
   */
  ActionFactory(Object steps) {
    this.steps = steps
    this.log = new LogClient(steps)
  }

  GithubAction makeAction(Map scmArgs) {
    String actionType = determineType(scmArgs)

    GithubAction action

    if (actionType == 'DockerAction') {
        action = new DockerAction(this.steps)
    }

    action.name = scmArgs.Name

    return action
  }

  private String determineType(Map scmArgs) {
    this.log.info("Checking out ${scmArgs.Org}/${scmArgs.Name}@${scmArgs.Ref}")

    String actionType

    String workspace = this.steps.env.WORKSPACE_TMP ?: this.steps.env.WORKSPACE

    this.steps.dir("${workspace}/${scmArgs.Name}") {
      this.steps.checkout(
        changelog: false,
        poll: false,
        scm: [
            $class: 'GitSCM', branches: [[name: scmArgs.Ref]],
            doGenerateSubmoduleConfigurations: false, extensions: [],
            submoduleCfg: [],
            userRemoteConfigs: [[url: "https://github.com/${scmArgs.Org}/${scmArgs.Name}"]]
        ]
      )

      actionType = this.steps.fileExists('Dockerfile') == true ? 'DockerAction' : 'nodeAction'
    }

    this.log.debug("The Github action type is ${actionType}.")

    return actionType
  }

}
