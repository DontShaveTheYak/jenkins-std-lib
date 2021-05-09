/* groovylint-disable BuilderMethodWithSideEffects, DuplicateStringLiteral, FactoryMethodName */
package org.dsty.github

import org.dsty.bash.BashClient
import org.dsty.bash.Result
import org.dsty.logging.LogClient
import com.cloudbees.groovy.cps.NonCPS

/**
* Github Action that uses a Dockerfile.
*/
class DockerAction implements GithubAction, Serializable {

  /**
   * Workflow script representing the jenkins build.
   */
  Object steps

  /**
   * Logging client
   */
  LogClient log

  /**
   * Bash Client
   */
  BashClient bash

  /**
   * The name of the action.
   */
  String name

  /**
   * Default Constructor
   * @param steps The workflow script representing the jenkins build.
   */
  DockerAction(Object steps) {
    this.steps = steps
    this.log = new LogClient(steps)
    this.bash = new BashClient(steps)
  }

  Map with(Map args) {
    this.build()

    String containerArgs = args.collect { "'${it.value}'" }.join(' ')

    String output = this.run(containerArgs)

    this.steps.println(cleanOutput(output))

    return parseOutputs(output)
  }

  void build() {
    this.log.info("Building Docker Container for ${this.name}")

    this.steps.dir(this.name) {
      Result result = this.bash.silent("docker build -t ${this.name} .")

      this.log.debug(result.output)
    }
  }

  String run(String containerArgs) {
    this.log.info("Running Action ${this.name}")

    String buildSlug = "${this.steps.env.BUILD_TAG}-${this.name}"

    Result result = this.bash.silent("docker run --rm --name ${buildSlug} ${this.name} ${containerArgs}")

    this.log.debug(result.stdOut)

    return result.stdOut
  }

  @NonCPS
  Map parseOutputs(String output) {
      Map outputs = [:]

      List matches = (output =~ /(?m)^::.*$/).findAll()

      for (match in matches) {
        String outputName = (match =~ /(?m)(?<=name=).*(?=::)/).findAll().first()
        String outputValue = (match =~ /(?m)::.*::(.*$)/).findAll().first()[1]

        outputs[outputName] = outputValue
      }

      return outputs
  }

  @NonCPS
  String cleanOutput(String output) {
      return (output =~ /(?m)^::.*$/).replaceAll('')
  }

}
