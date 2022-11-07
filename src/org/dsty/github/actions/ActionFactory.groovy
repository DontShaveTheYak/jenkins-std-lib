/* groovylint-disable DuplicateNumberLiteral, DuplicateStringLiteral, LineLength */
package org.dsty.github.actions

import org.dsty.logging.LogClient

/**
 * Determines the type of Action being used and returns the
 * proper class to represent that action.
 */
class ActionFactory implements Serializable {

    /**
     * Workflow script representing the jenkins build.
     */
    final private Object steps

    /**
     * Logging client
     */
    final private LogClient log

    /**
     * Default Constructor
     * <p>Using this Class directly in a Jenkins pipeline is an Advanced
     * use case. Most people should just use {@link org.dsty.github.actions.Step#Step Step}.
     * @param steps The workflow script representing the jenkins build.
     */
    ActionFactory(Object steps) {
        this.steps = steps
        this.log = new LogClient()
    }

    /**
     * Return the proper Github Action.
     * @param options The valid keys and values can be found
     * <a href="https://docs.github.com/en/actions/reference/workflow-syntax-for-github-actions#jobsjob_idsteps">here</a>.
     */
    GithubAction makeAction(Map options) {
        String actionType = this.determineType(options)

        GithubAction action

        switch (actionType) {
            case 'docker':
                action = new DockerAction(this.steps)
                break
            case 'run':
                action = new RunAction(this.steps)
                break
            case 'node12':
                action = new JavaScriptAction(this.steps)
                break
        }

        // Stupid workaround that will be fixed later
        if (actionType == 'node16') {
            action = new JavaScriptAction(this.steps)
        }

        if (!action) {
            throw new IllegalStateException('Unable to determine the type of Github action.')
        }

        action.options = options

        return action
    }

    /**
     * Parses the action's id into a Github Org, Repo and Ref.
     * @param actionID The id for the Github action you want to run.
     * @return The Github Org, Repo and Ref.
     */
    private Map parseActionID(String actionID) {
        List parts
        String fullName
        Map results = [:]

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

    /**
     * Determines the type of action to use.
     * @param options The valid keys and values can be found
     * <a href="https://docs.github.com/en/actions/reference/workflow-syntax-for-github-actions#jobsjob_idsteps">here</a>.
     * @return The name of the type of action to use.
     */
    private String determineType(Map options) {
        String actionType

        if (options.run) {
            actionType = 'run'
    } else {
            Map scmArgs = this.parseActionID(options.uses)

            options.actionID = scmArgs.Name

            Map metadata = readMetadata(scmArgs, options.workspace)
            options.metadata = metadata

            this.log.debug("Metadata for ${options.actionID}:\n${this.log.pprint(metadata)}")

            actionType = metadata.runs.using
        }

        this.log.debug("The Github action type is ${actionType}.")

        return actionType
    }

    /**
     * Reads the action Metadata file.
     * @param scmArgs The Github Org, Repo and Ref to checkout the action.
     * @param workspace The directory to store the actions code.
     * @return The actions metadata.
     */
    private Map readMetadata(Map scmArgs, String workspace) {
        Map metadata

        this.log.info("Checking out ${scmArgs.Org}/${scmArgs.Name}@${scmArgs.Ref}")

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

            String fileName = this.steps.fileExists('action.yml') ? 'action.yml' : 'action.yaml'

            metadata = this.steps.readYaml(file: fileName)
        }

        return metadata
    }

}
