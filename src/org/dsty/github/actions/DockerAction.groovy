/* groovylint-disable DuplicateStringLiteral, ThrowException, UnnecessaryCollectCall */
package org.dsty.github.actions

import org.dsty.bash.Result
import com.cloudbees.groovy.cps.NonCPS

/**
 * Github Action that uses a Docker Container.
 */
class DockerAction extends Action implements GithubAction {

    /**
     * The name of the action.
     */
    String name

    /**
     * The metadata from action.yml/yaml file.
     */
    protected Map metadata

    /**
     * Docker volume mounts for the container with out '-v' appended.
     */
    protected final List<String> mounts = []

    /**
     * Default Constructor
     * <p>Using this Class directly in a Jenkins pipeline is an Advanced
     * use case. Most people should just use {@link org.dsty.github.actions.Step#Step Step}.
     * @param steps The workflow script representing the jenkins build.
     */
    DockerAction(Object steps) {
        super(steps)
    }

    /**
     * Options are mix of values that come from either {@link org.dsty.github.actions.Step#Step Step}
     * or the {@link org.dsty.github.actions.ActionFactory#ActionFactory ActionFactory}. They why it's
     * prefered to use those classes over this one directly.
     * @param options used to configure and run this Github Action.
     */
    void setOptions(Map options) {
        this.options = options
        this.name = options.actionID
        this.metadata = options.metadata
    }

    /**
     * This method will prepare the Action, run the action and then parse the output.
     * @returns the outputs from the action.
     */
    Map run() {
        String imageID = this.dockerBuild()

        Map outputs = this.actionRun(imageID)

        return outputs
    }

    /**
     * Will either build the container image or use the
     * image specified in the {@link #metadata}.
     * @returns the docker image id.
     */
    String dockerBuild() {
        if (this.metadata.runs.image.contains('docker://')) {
            imageID = this.metadata.runs.image.replace('docker://', '')
            return imageID
        }

        this.log.info("Building Docker Container for ${this.name}")

        this.steps.dir("${this.options.workspace}/${this.name}") {
            Result result = this.bash.silent("docker build -t ${this.name} .")

            this.log.debug(result.output)
        }

        return this.name
    }

    /**
     * Runs the action using the values configued in
     * {@link #metadata metadata.runs}
     * @param imageID the docker image to use.
     * @returns the outputs from the action.
     */
    Map actionRun(String imageID) {
        Map inputs = this.loadDefaults()

        this.log.info("Running Action ${this.name}")

        List runArgs = metadata.runs.get('args', [])

        String containerArgs =  runArgs ? this.renderArgs(runArgs, inputs) : ''

        Map renderedEnvVars = inputs.collectEntries { key, value ->
            [("INPUT_${ this.normalizeVariable(key) }".toString()): value]
        }

        Map finalEnvVars = renderedEnvVars << this.options.env

        String buildSlug = "${this.steps.env.BUILD_TAG}-${this.name}".replaceAll(' ', '-')

        String entryFlag = '--entrypoint'

        String entryPoint = this.metadata.runs.entrypoint ? "${entryFlag} ${this.metadata.runs.entrypoint}" : ''

        Map outputs = [:]

        if (this.metadata.runs['pre-entrypoint']) {
            this.log.debug("${this.name} - Running pre-entrypoint.")

            String preArgs = "${this.metadata.runs['pre-entrypoint']} ${containerArgs}"

            outputs << this.dockerRun("${buildSlug}-pre", imageID, finalEnvVars, preArgs, entryPoint)
        }

        this.log.debug("${this.name} - Running main entrypoint.")

        outputs << this.dockerRun(buildSlug, imageID, finalEnvVars, containerArgs, entryPoint)

        if (this.metadata.runs['post-entrypoint']) {
            this.log.debug("${this.name} - Running post-entrypoint.")

            String postArgs = "${this.metadata.runs['post-entrypoint']} ${containerArgs}"

            outputs << this.dockerRun("${buildSlug}-pre", imageID, finalEnvVars, postArgs, entryPoint)
        }

        return outputs
    }

    /**
     * Runs a docker container and parses it's outputs.
     * @param containerName the name of the container.
     * @param imageID the docker image to use.
     * @param envVars the environment vars to pass to docker run.
     * @param containerArgs the argurments to pass to the entrypoint.
     * @param entryPoint the entrypoint to use or an empty string to use containers entrypoint.
     * @returns the outputs from the action.
     */
    Map dockerRun(String containerName, String imageID, Map finalEnvVars, String containerArgs, String entryPoint) {
        String envVars = finalEnvVars.collect { key, value ->
            "-e ${key}"
        }.join(' ')

        String containerEnv = finalEnvVars.collect { key, value ->
            "'${key}=${value }'"
        }.join(' ')

        this.mounts.addAll(
            [
                '/var/run/docker.sock:/var/run/docker.sock',
                "${this.steps.WORKSPACE}:/github/workspace"
            ]
        )

        String volumeMounts = this.mounts.collect { volume -> "-v '${volume}'" }.join(' ')

        String script = """\
        env ${containerEnv} docker run --rm --name ${containerName} \
            --group-add \$(getent group docker | cut -d: -f3) \
            ${envVars} \
            ${volumeMounts} \
            ${entryPoint} \
            ${imageID} \
            ${containerArgs}
        """.stripIndent()

        Result result = this.bash.silent(script)

        this.log.debug(result.stdOut)

        this.steps.println(this.cleanOutput(result.stdOut))

        return this.parseOutputs(result.stdOut)
    }

    /**
     * Parses the metadata for inputs and sets the default value
     * or used the value from {@link #options}.
     * @returns the configued inputs.
     */
    Map loadDefaults() {
        Map inputs = [:]

        if (this.metadata.inputs) {
            inputs = this.metadata.inputs.collectEntries { inputName, inputMeta ->
                String defaultValue = inputMeta.default ?: ''

                String finalValue = this.options.with["${inputName}"] ?: defaultValue

                if (!finalValue && inputMeta.required) {
                    finalValue = 'REQUIRED'
                }

                [(inputName): finalValue]
            }
        }

        List required = inputs.findAll { key, value ->
            value == 'REQUIRED'
        }.collect { key, value ->
            key
        }

        if (required) {
            throw new Exception("No value was provided for these required inputs: ${required.join(', ')}")
        }

        Map filteredInputs = inputs.findAll { key, value ->
            value != 'REQUIRED'
        }

        return filteredInputs
    }

    /**
     * Creates a string of arguments that can be passed to a shell.
     * @param args that are defined in {@link #metadata metadata.runs.args} but can also be an empty list.
     * @param inputs the configued inputs from {@link #loadDefaults()}.
     * @returns the command line arguments.
     */
    String renderArgs(List args, Map inputs) {
        List renderedArgs = args.collect { arg ->
            renderTemplate(arg, inputs)
        }

        String containerArgs = renderedArgs.collect { arg ->
            "'${arg}'"
        }.join(' ')

        return containerArgs
    }

    /**
     * Tranforms inputs into environment variables using the format Github actions requires.
     * @param inputs the configued inputs from {@link #loadDefaults()}.
     * @returns the environment vars in the correct format.
     */
    String renderEnvVars(Map inputs) {
        return inputs.collectEntries { key, value ->
            [("INPUT_${normalizeVariable(key)}".toString()): value]
        }
    }

    /**
     * Makes a string match the format required
     * by Github Actions for use as an input Map key.
     * @param value the value to be transformed.
     * @returns the value that is now all uppercase and spaces turned to _.
     */
    @NonCPS
    String normalizeVariable(String value) {
        return value.toUpperCase().replace(' ', '_')
    }

    /**
     * Replaces all Github variables with Groovy GString variables.
     * @param value that contains Github Variables.
     * @returns the value but with GString variables.
     */
    @NonCPS
    String convertVariable(String value) {
    /* groovylint-disable-next-line GStringExpressionWithinString */
        return (value =~ /\$\{\{(.*)}}/).replaceAll('\\${$1}')
    }

    /**
     * Changes dot notation variables to bracket notation.
     * <p>This change is important because in the Github Actions
     * metadata file variables often have hypens. Hypens don't seem
     * to work with dot notation so we reformat them to bracket notation.
     * @param template that contains GStrings with dot notation.
     * @returns the template with GString that use bracket notation instead.
     */
    @NonCPS
    String normalizeTemplate(String template) {
        String templateVar = (template =~ /(?<=inputs\.)[\w\-]*/).findAll().first()

        return (template =~ /\.[\w\-]*/).replaceFirst("['${templateVar}']")
    }

    /**
     * Takes a String that contains GString variables
     * and then renders that String into one where the
     * variables have been replaced with their values.
     * @param arg that contains GString Variables.
     * @param inputs the variables used when rendering the String.
     * @returns the arg but with GString variables rendered into values.
     */
    @NonCPS
    String renderTemplate(String arg, Map inputs) {
        if (! (arg =~ /\$\{.*}/) ) {
            return arg
        }

        String template = convertVariable(arg)
        String normalTemplate = normalizeTemplate(template)

        groovy.text.SimpleTemplateEngine engine = new groovy.text.SimpleTemplateEngine()

        return engine.createTemplate(normalTemplate).make(['inputs': inputs]).toString()
    }

}
