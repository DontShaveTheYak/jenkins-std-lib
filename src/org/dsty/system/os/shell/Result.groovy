package org.dsty.system.os.shell

import com.cloudbees.groovy.cps.NonCPS

/**
 * Contains the results of a {@link Shell} command execution.
 */
class Result implements Serializable {

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

    Result(String stdOut, String stdErr, String output, Integer exitCode) {
        this.stdOut = stdOut
        this.stdErr = stdErr
        this.output = output
        this.exitCode = exitCode
    }

    /**
     * Print the output of the command execution when the {@link Result} is printed.
     * @return The output from the command execution.
     */
    @Override
    @NonCPS
    String toString() {
        return this.output
    }

}
