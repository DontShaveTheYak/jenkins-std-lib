/* groovylint-disable DuplicateStringLiteral */
package org.dsty.github.actions

import org.dsty.bash.BashClient
import org.dsty.logging.LogClient
import com.cloudbees.groovy.cps.NonCPS

/**
 * Github Action that uses a Dockerfile.
 */
class Action implements Serializable {

    /**
     * Workflow script representing the jenkins build.
     */
    protected Object steps

    /**
     * Logging client
     */
    protected LogClient log

    /**
     * Bash Client
     */
    protected BashClient bash

    /**
     * The options from the {@link org.dsty.github.actions.Step#call(java.util.Map) Step}.
     */
    protected Map options

    /**
     * Default Constructor
     * <p>Using this Class directly in a Jenkins pipeline is an Advanced
     * use case. Most people should just use {@link org.dsty.github.actions.Step#Step Step}.
     * @param steps The workflow script representing the jenkins build.
     */
    Action(Object steps) {
        this.steps = steps
        this.log = new LogClient()
        this.bash = new BashClient(steps)
    }

    /**
     * Parses the input for Github Actions outputs.
     * @param input to search for Github Actions outputs.
     * @returns the outputs found.
     */
    @NonCPS
    Map parseOutputs(String input) {
        Map outputs = [:]

        List matches = (input =~ /(?m)^::.*$/).findAll()

        for (match in matches) {
            String outputName = (match =~ /(?m)(?<=name=).*(?=::)/).findAll().first()
            String outputValue = (match =~ /(?m)::.*::(.*$)/).findAll().first()[1]

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
    String cleanOutput(String input) {
        return (input =~ /(?m)^::.*$/).replaceAll('')
    }

}
