package org.dsty.system.os.shell

/**
 * Exception thrown when a {@link Shell} command results in an error.
 */
class ExecutionException extends Exception {

    /**
     * The contents of stdOut from the command execution.
     */
    String stdOut

    /**
     * The contents of stdErr from the command execution.
     */
    String stdErr

    /**
     * The combined contents of stdOut and stdErr from the command execution.
     */
    String output

    /**
     * The exitCode from the command execution.
     */
    Integer exitCode

    ExecutionException(String stdOut, String stdErr, String output, Integer exitCode) {
        super("Script exitCode was ${exitCode} and stderr:\n${stdErr}")
        this.stdOut = stdOut
        this.stdErr = stdErr
        this.output = output
        this.exitCode = exitCode
    }

    ExecutionException(String stdOut, String stdErr, String output, Integer exitCode, Throwable cause) {
        super("Script exitCode was ${exitCode} and stderr:\n${stdErr}", cause)
        this.stdOut = stdOut
        this.stdErr = stdErr
        this.output = output
        this.exitCode = exitCode
    }

    /**
     * Similar to printing the exception but shows the command output
     * instead of just the <code>stdErr</code>.
     */
    String getFullMessage() {
        return "Script exitCode was ${this.exitCode}. Output was:\n${this.output}"
    }

}
