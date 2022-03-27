/* groovylint-disable DuplicateStringLiteral, Println */
package org.dsty.logging

import static groovy.json.JsonOutput.prettyPrint
import static groovy.json.JsonOutput.toJson
import static org.dsty.jenkins.Instance.pluginInstalled
import org.dsty.jenkins.Build

import com.cloudbees.groovy.cps.NonCPS

/**
 * Basic logger that uses the <a href="https://plugins.jenkins.io/ansicolor/">AnsiColor</a>
 * plugin to output log messages in color.
 * <p>
 * Set the environment variable <code>PIPELINE_LOG_LEVEL</code> to
 * <code>DEBUG</code>, <code>INFO</code>, <code>WARN</code> or <code>ERROR</code> to
 * control logger output. Setting it to any other value will stop
 * all output. If unset it defaults to <code>INFO</code>.
 */
class LogClient implements Serializable {

    /**
     * Workflow script representing the jenkins build.
     */
    private Object steps

    /**
     * If we should print in color.
     */
    final private Boolean printColor

    /**
     * Default Constructor
     * <p>
     * Example:
     * <pre>{@code
     * import org.dsty.logging.LogClient
     *LogClient log = new LogClient()
     * }</pre>
     */
    LogClient() {
        this.printColor = useColor()
    }

    /**
     * Load the workflow steps from the current Build.
     */
    Object getSteps() {

        if (!this.@steps) {
            final Build currentBuild = new Build()
            /* groovylint-disable-next-line UnnecessaryGetter */
            this.steps = currentBuild.getWorkFlowScript()
        }

        return this.@steps
    }

    /**
     * Logs a message to the console in <font color="green">green</font>.
     * @param input The item you want to output to console.
     */
    void debug(Object input) {
        if (levelCheck(['DEBUG'])) {
            writeMsg("[Debug] ${getString(input)}", '32')
        }
    }

    /**
     * Logs a message to the console in <font color="blue">blue</font>.
     * @param input The item you want to output to console.
     */
    void info(Object input) {
        if (levelCheck(['DEBUG', 'INFO'])) {
            writeMsg("[Info] ${getString(input)}", '34')
        }
    }

    /**
     * Logs a message to the console in <font color="#C4A000">yellow</font>.
     * @param input The item you want to output to console.
     */
    void warn(Object input) {
        if (levelCheck(['DEBUG', 'INFO', 'WARN'])) {
            writeMsg("[Warning] ${getString(input)}", '33')
        }
    }

    /**
     * Logs a message to the console in <font color="red">red</font>.
     * @param input The item you want to output to console.
     */
    void error(Object input) {
        if (levelCheck(['DEBUG', 'INFO', 'WARN', 'ERROR'])) {
            writeMsg("[Error] ${getString(input)}", '31')
        }
    }

    /**
     * Returns a Map or List as a pretty JSON String.
     * <p>
     * Example:
     * <pre>{@code
     * Map test = [:]
     *test['List'] = [1,2,3,4]
     *log.debug(log.pprint(test))
     * }</pre>
     * Results:<font color="green">
     * <pre>{@code
     * [Debug] {
     * "List": [
     *     1,
     *     2,
     *     3,
     *     4
     *  ]
     *}
     * }</pre></font>
     * @param item The List or Map you want to format as pretty JSON.
     * @return A JSON String that is pretty.
     */
    String pprint(Object item) {
        return prettyPrint(toJson(item))
    }

    String writeMsg(Object input, String colorCode) {
        if (this.printColor) {
            this.steps.ansiColor('xterm') {
                this.steps.println("\u001B[${colorCode}m${input}\u001B[0m")
            }
        } else {
            this.steps.println(input)
        }
    }

    /**
     * Check if the current level should be logged.
     * @param levels The levels that you want to log to.
     * @return <code>true</code> If the current level is in
     * levels param and <code>false</code> if not.
     */
    private Boolean levelCheck(List levels) {
        String level = this.steps.env.PIPELINE_LOG_LEVEL ?: 'INFO'
        return levels.contains(level)
    }

    /**
     * Returns a string of the input object
     * @param input Any object.
     * @return The string version of the object.
     */
    private String getString(Object input) {
        return input.toString()
    }

    @NonCPS
    private Boolean useColor() {
        return pluginInstalled('ansicolor')
    }

}
