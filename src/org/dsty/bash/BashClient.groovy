package org.dsty.bash

import org.dsty.bash.Result
import org.dsty.logging.LogClient
import org.dsty.bash.ScriptError

/**
 * Bash Client
 */
class BashClient implements Serializable {

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
  BashClient(Object steps) {
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
  String formatScript(String userScript, Boolean consoleOutput=true, Boolean failFast=true) {
    String teeOutput = 'exec 2> >(tee -a stderr stdall) 1> >(tee -a stdout stdall)'
    String exec = consoleOutput ? teeOutput : "exec 3>/dev/null 2> >(tee -a stderr stdall >&3) 1> >(tee -a stdout stdall >&3)"

    String script = """\
    #!/bin/bash
    source \$HOME/.bashrc > /dev/null 2>&1 || true
    { ${failFast ? 'set -e;' : 'set +e;'} } > /dev/null 2>&1
    ${exec}
    { ${this.steps.env.PIPELINE_LOG_LEVEL == 'DEBUG' ? 'set -x;' : 'set +x;' } } > /dev/null 2>&1
    
    # User Script
    """.stripIndent()
    
    userScript = userScript.stripIndent()
    script = "${script}${userScript}"
    this.log.debug("Formatted script:\n${script}")

    return script
  }

  /**
  * Runs a bash script that sends all output to the console
  * and returns a result object with the stdOut, stdErr
  * exitCode and the combined output of the script.
  * @param userScript The bash command or script you want to run.
  * @return The results of the bash command or script.
  */
  Result call(String userScript) {
    String script = formatScript(userScript)

    return execute(script)
  }

  /**
  * Runs a bash script that sends no output to the console
  * and returns a result object with the stdOut, stdErr
  * exitCode and the combined output of the script.
  * @param userScript The bash command or script you want to run.
  * @return The results of the bash command or script.
  */
  Result silent(String userScript) {
    String script = formatScript(userScript, false)

    return execute(script)
  }

  /**
  * Runs a bash script that ignores errors.
  * @param userScript The bash command or script you want to run.
  * @param consoleOutput Determines if output is sent to the console.
  * @param failFast Determines if script should stop on first error or not.
  * @return The results of the bash command or script.
  */
  Result ignoreErrors(String userScript, Boolean consoleOutput=true, Boolean failFast=false) {
    String script = formatScript(userScript, consoleOutput, failFast)
    Result result

    try {
      result = execute(script)
    } catch (ScriptError ex) {
      result = new Result(ex.stdOut, ex.stdErr, ex.output, ex.exitCode)
    }

    return result
  }

  /**
  * Executes a bash script.
  * @param script The bash command or script you want to run.
  * @return The results of the bash command or script.
  */
  private Result execute(String script) {
    Integer exitCode = this.steps.sh(script: script, returnStatus: true)

    def(String stdOut, String stdErr, String output) = readOutputs()

    if (exitCode) {
      throw new ScriptError(stdOut, stdErr, output, exitCode)
    }

    return new Result(stdOut, stdErr, output, exitCode)
  }

  /**
  * Reads the script outputs from files and then removes them.
  * @return The stdout, stderr and combined script output.
  */
  private List<String> readOutputs() {
    String stdOut = this.steps.readFile('stdout')
    String stdErr = this.steps.readFile('stderr')
    String output = this.steps.readFile('stdall')

    this.steps.sh('#!/bin/bash\n{ set +x; } > /dev/null 2>&1 \nrm stdout stderr stdall > /dev/null 2>&1 || true')

    List results = [stdOut, stdErr, output].collect { it.replaceAll('(?m)^\\+.*(?:\r?\n)?', '').trim() }

    return results
  }

}
