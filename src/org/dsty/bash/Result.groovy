package org.dsty.bash

import com.cloudbees.groovy.cps.NonCPS

/**
 * Contains the results of a bash script execution.
 * @deprecated As of release 0.10.0, replaced by {@link org.dsty.system.os.shell.Result org.dsty.system.os.shell.Result}
 */
@Deprecated
class Result implements Serializable {

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

    Result(String stdOut, String stdErr, String output, Integer exitCode) {
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
