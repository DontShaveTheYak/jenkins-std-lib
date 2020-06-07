package com.dsty.exceptions

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

  /**
   * The errorMessage used when printing the exception.
   */
  String errorMessage

  ScriptError(String stdOut, String stdErr, String output, Integer exitCode) {
    this.stdOut = stdOut
    this.stdErr = stdErr
    this.output = output
    this.exitCode = exitCode
    this.errorMessage = "Script exited ${this.exitCode}. stderr was:\n${this.stdErr}"
  }

  String getFullMessage() {
      return "Script exited ${this.exitCode}. Output was:\n${this.output}"
  }

  public String getErrorMessage() {
      return this.errorMessage;
  }
}