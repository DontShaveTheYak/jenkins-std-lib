/* groovylint-disable DuplicateStringLiteral */
package org.dsty.github

import org.dsty.logging.LogClient

/**
 * Runs Github Actions.
 */
class Action implements Serializable {

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
  Action(Object steps) {
    this.steps = steps
    this.log = new LogClient(steps)
  }

  /**
  * Formats a bash script by adding the shebang,
  * setting the verbose level and sourcing bashrc.
  * @param userScript The bash script you want to run.
  * @param consoleOutput If you want the script results
  *                      to be printed out to console.
  * @param failFast If you want the script to stop on first
  *                 non zero exit code.
  * @return The userScript formatted for bash.
  */
  GithubAction uses(String name) {
    this.log.info("Preparing Github action ${name}.")

    Map scmArgs = parseName(name)

    ActionFactory factory = new ActionFactory(this.steps)

    GithubAction action = factory.makeAction(scmArgs)

    return action
  }

  private static Map parseName(String name) {
    Map results = [:]

    String fullName
    List parts

    if (name.contains('@')) {
      parts = name.tokenize('@')
      fullName = parts[0]
      results.Ref = parts[1]
    }

    parts = fullName ? fullName.tokenize('/') : name.tokenize('/')

    results.Name = parts[1]
    results.Org = parts[0]
    results.Ref = results.Ref ?: 'master'

    return results
  }

}
