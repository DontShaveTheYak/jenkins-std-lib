package org.dsty.exceptions

/**
 * Custom Exception thrown by the bash global variable.
 */
class ScriptError extends Exception {

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

  ScriptError(String stdOut, String stdErr, String output, Integer exitCode) {
    super("Script exitCode was ${exitCode} and stderr:\n${stdErr}")
    this.stdOut = stdOut
    this.stdErr = stdErr
    this.output = output
    this.exitCode = exitCode
  }

  String getFullMessage() {
      return "Script exitCode was ${this.exitCode}. Output was:\n${this.output}"
  }

}
