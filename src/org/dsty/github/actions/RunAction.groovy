package org.dsty.github.actions

import org.dsty.bash.Result

/**
* Github Action that uses a executes a shell command.
* <p> Currently the working-directory and shell options are
* not implemented.</p>
*/
class RunAction extends Action implements GithubAction {

  /**
   * The name of the action.
   */
  String name

  /**
   * The options from the {@link org.dsty.github.actions.Step#call(java.util.Map) Step}.
   */
  Map options

  /**
   * Default Constructor
   * <p>Using this Class directly in a Jenkins pipeline is an Advanced
   * use case. Most people should just use {@link org.dsty.github.actions.Step#Step Step}.
   * @param steps The workflow script representing the jenkins build.
   */
  RunAction(Object steps) {
    super(steps)
  }

  /**
   * Options are mix of values that come from either {@link org.dsty.github.actions.Step#Step Step}
   * or the {@link org.dsty.github.actions.ActionFactory#ActionFactory ActionFactory}. Thats why it's
   * prefered to use those classes over this one directly.
   * @param options used to configure and run this Github Action.
   */
  void setOptions(Map options) {
    this.options = options
    this.name = options.name ?: options.run
  }

  /**
   * This method will prepare the Action, run the action and then parse the output.
   * @returns the outputs from the action.
   */
  @Override
  Map run() {

    Map outputs = this.runCMD()

    return outputs

  }

  /**
   * Runs the action using the provided command.
   * @returns the outputs from the action.
   */
  Map runCMD() {

    this.log.info("Run ${this.name}")

    this.options.env = this.options.env ?: [:]

    /* groovylint-disable-next-line SpaceAfterOpeningBrace, SpaceBeforeClosingBrace */
    List containerEnv = this.options.env.collect {"${it.key}=${it.value }"}

    Map outputs = [:]

    this.steps.withEnv(containerEnv) {

      Result result = this.bash.silent(this.options.run)

      this.log.debug(result.stdOut)

      this.steps.println(this.cleanOutput(result.stdOut))

      outputs = this.parseOutputs(result.stdOut)

    }

    return outputs

  }

}
