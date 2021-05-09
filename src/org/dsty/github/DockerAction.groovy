/* groovylint-disable BuilderMethodWithSideEffects, DuplicateStringLiteral, FactoryMethodName, ThrowException, UnnecessaryCollectCall */
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

  Map with(Map args = [:]) {

    Map metadata

    String workspace = this.steps.env.WORKSPACE_TMP ?: this.steps.env.WORKSPACE

    this.steps.dir("${workspace}/${this.name}") {

      metadata = this.loadMetadata()

      this.build()

    }

    Map inputs = this.loadDefaults(args, metadata)

    List runArgs = metadata.get('runs', [:]).get('args', [])

    String containerArgs =  runArgs ? this.renderArgs(runArgs, inputs) : ''

    String output = this.run(containerArgs, inputs)

    this.steps.println(cleanOutput(output))

    return this.parseOutputs(output)
  }

  void build() {

    this.log.info("Building Docker Container for ${this.name}")

    Result result = this.bash.silent("docker build -t ${this.name} .")

    this.log.debug(result.output)

  }

  String run(String containerArgs, Map inputs) {

    String buildSlug = "${this.steps.env.BUILD_TAG}-${this.name}".replaceAll(' ', '-')

    Map renderedEnvVars = inputs.collectEntries { [("INPUT_${ normalizeVariable(it.key) }".toString()): it.value] }

    String envVars = renderedEnvVars.collect { "-e ${it.key}" }.join(' ')

    /* groovylint-disable-next-line SpaceAfterOpeningBrace, SpaceBeforeClosingBrace */
    List containerEnv = renderedEnvVars.collect {"${it.key}=${it.value }"}

    this.log.info("Running Action ${this.name}")

    Result result

    this.steps.withEnv(containerEnv) {

      result = this.bash.silent("docker run --rm --name ${buildSlug} ${envVars} ${this.name} ${containerArgs}")

    }

    this.log.debug(result.stdOut)

    return result.stdOut

  }

  Map loadMetadata() {

    this.log.debug("Loading metadata for ${this.name}")

    String metadataFile = this.steps.fileExists('action.yml') == true ? 'action.yml' : 'action.yaml'

    if (!this.steps.fileExists(metadataFile)) {
      throw new Exception("Could not locate action.yml/yml metadata file for ${this.name}")
    }

    Map metadata = this.steps.readYaml(file: metadataFile)

    this.log.debug(metadata)

    return metadata

  }

  Map loadDefaults(Map userArgs, Map metadata) {

    Map inputs = [:]

    if (metadata.inputs) {
      inputs = metadata.inputs.collectEntries { inputName, inputMeta ->
        String defaultValue = inputMeta.default ?: ''

        String finalValue = userArgs["${inputName}"] ?: defaultValue

        if (!finalValue && inputMeta.required) {
          finalValue = 'REQUIRED'
        }

        [(inputName): finalValue]
      }
    }

    List required = inputs.findAll { it.value == 'REQUIRED' }.collect { it.key }

    if (required) {
      throw new Exception("No value was provided for these required inputs: ${required.join(', ')}")
    }

    Map filteredInputs = inputs.findAll { it.value != 'REQUIRED' }

    return filteredInputs

  }

  String renderArgs(List args, Map inputs) {

    List renderedArgs = args.collect { it -> renderTemplate(it, inputs) }

    String containerArgs = renderedArgs.collect { "'${it.value}'" }.join(' ')

    return containerArgs

  }

  String renderEnvVars(Map inputs) {

    return inputs.collectEntries { [("INPUT_${normalizeVariable(it.key)}".toString()): it.value] }

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

  @NonCPS
  String normalizeVariable(String inputName) {

    return inputName.toUpperCase().replace(' ', '_')

  }

  @NonCPS
  String convertVariable(String arg) {

    /* groovylint-disable-next-line GStringExpressionWithinString */
    return (arg =~ /\$\{\{(.*)}}/).replaceAll('\\${$1}')

  }

  @NonCPS
  String normalizeTemplate(String template) {

    String templateVar = (template =~ /(?<=\.)[\w\-]*/).findAll().first()

    return (template =~ /\.[\w\-]*/).replaceFirst("['${templateVar}']")

  }

  @NonCPS
  String renderTemplate(String arg, Map inputs) {

    String template = convertVariable(arg)
    String normalTemplate = normalizeTemplate(template)

    groovy.text.SimpleTemplateEngine engine = new groovy.text.SimpleTemplateEngine()

    return engine.createTemplate(normalTemplate).make(['inputs': inputs]).toString()

  }

}
