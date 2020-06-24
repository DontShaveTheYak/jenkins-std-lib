import org.dsty.exceptions.ScriptError
import com.cloudbees.groovy.cps.NonCPS

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
String formatScript(String userScript, Boolean consoleOutput=true, Boolean failFast=true){

  String exec = consoleOutput ? 'exec 2> >(tee -a stderr stdall) 1> >(tee -a stdout stdall)' : 'exec 3>/dev/null 2> >(tee -a stderr stdall >&3) 1> >(tee -a stdout stdall >&3)'

  String script = """\
#!/bin/bash
source \$HOME/.bashrc > /dev/null 2>&1 || true
{ ${failFast ? 'set -e;' : 'set +e;'} } > /dev/null 2>&1
${exec}
{ ${env.PIPELINE_LOG_LEVEL == 'DEBUG' ? 'set -x;' : 'set +x;' } } > /dev/null 2>&1
${userScript.stripIndent()}
  """.stripIndent()
  log.debug("Formatted script:\n${script}")

  return script
}

/**
 * Contains the results of a bash script execution.
 */
class Result {

  /**
   * The contents of stdOut from the bash script.
   */
  String stdOut

  /**
   * The contents of stdErr from the bash script.
   */
  String stdErr

  /**
   * The combined contents of stdOut and stdErr from the bash script.
   */
  String output

  /**
   * The exitCode from the bash script.
   */
  Integer exitCode

  Result(String stdOut, String stdErr, String output, Integer exitCode){
    this.stdOut = stdOut
    this.stdErr = stdErr
    this.output = output
    this.exitCode = exitCode
  }

  /**
   * Print the output of the bash script when the class is printed.
   * @return The output from the bash script.
   */
  @Override
  @NonCPS
  String toString() {
      return this.output
  }
}

/**
 * Runs a bash script that sends all output to the console
 * and returns a result object with the stdOut, stdErr
 * exitCode and the combined output of the script.
 * @param userScript The bash command or script you want to run.
 * @return The results of the bash command or script.
 */
Result call(String userScript){
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
Result silent(String userScript){
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
Result ignoreErrors(String userScript, Boolean consoleOutput=true, Boolean failFast=false){
  String script = formatScript(userScript, consoleOutput, failFast)
  Result result

  try{
    result = execute(script)
  }catch(ScriptError ex){
    result = new Result(ex.stdOut, ex.stdErr, ex.output, ex.exitCode)
  }

  return result
}


/**
 * Executes a bash script.
 * @param script The bash command or script you want to run.
 * @return The results of the bash command or script.
 */
private Result execute(String script){
  Integer exitCode = sh(script: script, returnStatus: true)

  def(String stdOut, String stdErr, String output) = getOutputs()

  if(exitCode){
    throw new ScriptError(stdOut, stdErr, output, exitCode)
  }
  
  return new Result(stdOut, stdErr, output, exitCode)
}

/**
 * Reads the script outputs from files and then removes them.
 * @return The stdout, stderr and combined script output.
 */
private List<String> getOutputs(){
  String stdOut = readFile('stdout')
  String stdErr = readFile('stderr')
  String output = readFile('stdall')

  sh('#!/bin/bash\n{ set +x; } > /dev/null 2>&1 \nrm stdout stderr stdall > /dev/null 2>&1 || true')

  List results = [stdOut, stdErr, output].collect { it.replaceAll('(?m)^\\+.*(?:\r?\n)?', '').trim() }

  return results
}
