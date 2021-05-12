/* groovylint-disable DuplicateStringLiteral */
package org.dsty.github.actions

import org.dsty.logging.LogClient

/**
 * Runs Github Actions.
 */
class Step implements Serializable {

  /**
   * Workflow script representing the jenkins build.
   */
  private final Object steps

  /**
   * Logging client
   */
  private final LogClient log

  private GithubAction action

  /**
   * Default Constructor
   * @param steps The workflow script representing the jenkins build.
   */
  Step(Object steps) {
    this.steps = steps
    this.log = new LogClient(steps)
  }

  /**
  * Handles the fetching and setup of a Github Action.
  * @param name The name of the action to setup.
  * @return The action that is now ready to run.
  */
  Step uses(String name) {
    this.log.info("Preparing Github action ${name}.")

    Map scmArgs = parseName(name)

    ActionFactory factory = new ActionFactory(this.steps)

    this.action = factory.makeAction(scmArgs)

    return this
  }

  /**
  * Run the action with an optional Map of inputs.
  * @param inputs The key,value pairs that will be passed to the action.
  * @return The outputs from the action.
  */
  Map with(Map inputs = [:]) {

    return this.action.with(inputs)

  }

  /**
  * Parses the action's id into a Github Org, Repo and Ref.
  * @param actionID The id for the Github action you want to run.
  * @return The Github Org, Repo and Ref.
  */
  private static Map parseName(String actionID) {
    Map results = [:]

    String fullName
    List parts

    if (actionID.contains('@')) {
      parts = actionID.tokenize('@')
      fullName = parts[0]
      results.Ref = parts[1]
    }

    parts = fullName ? fullName.tokenize('/') : actionID.tokenize('/')

    results.Name = parts[1]
    results.Org = parts[0]
    results.Ref = results.Ref ?: 'master'

    return results
  }

}
