package org.dsty.github.actions

import org.dsty.logging.LogClient

/**
 * Runs a Github Actions step.
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

  /**
   * Default Constructor
   * <pre>{@code
   * import org.dsty.github.actions.Step
   *node() {
   *  Step action = new Step(this)
   *  Map options = [
   *      'uses': 'actions/hello-world-docker-action@master',
   *      'with': [
   *          'who-to-greet': 'Mona the Octocat'
   *      ]
   *  ]
   *  action(options)
   *&#125;}</pre>
   * @param steps The workflow script representing the jenkins build.
   */
  Step(Object steps) {
    this.steps = steps
    this.log = new LogClient(steps)
  }

  /**
   * Runs the Github Action Step.
   * @param options The valid keys and values can be found the <a href="https://docs.github.com/en/actions/reference/workflow-syntax-for-github-actions#jobsjob_idsteps">here</a>.
   * @return The outputs from the Action.
   */
  Map call(Map options) {

    options.env = options.env ?: [:]

    if (options.if == null) {

      options.if = true

    }

    if (!options.if) {

      this.log.info("Skipping ${options.stepName}.")
      return [:]
    }

    options.workspace = this.steps.env.WORKSPACE_TMP ?: this.steps.env.WORKSPACE

    ActionFactory factory = new ActionFactory(this.steps)

    GithubAction action = factory.makeAction(options)

    Map outputs = action.run()

    return outputs

  }

}
