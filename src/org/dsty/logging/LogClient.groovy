/* groovylint-disable DuplicateStringLiteral, Println */
package org.dsty.logging

import static groovy.json.JsonOutput.toJson
import static groovy.json.JsonOutput.prettyPrint

/**
 * Basic logger that uses the <a href="https://plugins.jenkins.io/ansicolor/">AnsiColor</a>
 * plugin to output log messages in color.
 * <p>
 * Set the environment variable {@code PIPELINE_LOG_LEVEL} to
 * {@code DEBUG}, {@code INFO}, {@code WARN} or {@code ERROR} to
 * control logger output. Setting it to any other value will stop
 * all output. If unset it defaults to {@code INFO}.
 */
class LogClient implements Serializable {

  /**
   * Workflow script representing the jenkins build.
   */
  Object steps

  /**
   * Default Constructor
   * <p>
   * Example:
   * <pre>{@code
   * import org.dsty.logging.LogClient
   *LogClient log = new LogClient(this)
   * }</pre>
   * @param steps The workflow script representing the jenkins build.
   */
  LogClient(Object steps) {
    this.steps = steps
  }

  /**
  * Logs a message to the console in <font color="green">green</font>.
  * @param input The item you want to output to console.
  */
  void debug(Object input) {
    if (levelCheck(['DEBUG'])) {
      this.steps.ansiColor('xterm') {
        this.steps.println("\u001b[32m[Debug] ${getString(input)}\u001b[0m")
      }
    }
  }

  /**
  * Logs a message to the console in <font color="blue">blue</font>.
  * @param input The item you want to output to console.
  */
  void info(Object input) {
    if (levelCheck(['DEBUG', 'INFO'])) {
      this.steps.ansiColor('xterm') {
        this.steps.println("\u001B[34m[Info] ${getString(input)}\u001B[0m")
      }
    }
  }

  /**
  * Logs a message to the console in <font color="#C4A000">yellow</font>.
  * @param input The item you want to output to console.
  */
  void warn(Object input) {
    if (levelCheck(['DEBUG', 'INFO', 'WARN'])) {
      this.steps.ansiColor('xterm') {
        this.steps.println("\u001B[33m[Warning] ${getString(input)}\u001B[0m")
      }
    }
  }

  /**
  * Logs a message to the console in <font color="red">red</font>.
  * @param input The item you want to output to console.
  */
  void error(Object input) {
    if (levelCheck(['DEBUG', 'INFO', 'WARN', 'ERROR'])) {
      this.steps.ansiColor('xterm') {
        this.steps.println("\u001B[31m[Error] ${getString(input)}\u001B[0m")
      }
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

  /**
  * Check if the current level should be logged.
  * @param levels The levels that you want to log to.
  * @return <code>true</code> If the current level is in
  *         levels param and <code>false</code> if not.
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

}
