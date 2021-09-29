package org.dsty.system.os.shell

/**
 * A shell is capable of running commands with a variety
 * of behaviors. It also handles input/output and generic
 * error handling for the user.
 */
interface Shell extends Serializable {

    /**
     * Execute the cmd.
     *
     * @param cmd     The full command and arguments you want the <code>Shell</code>
     *                to execute.
     * @return        A {@link Result} that contains the execution details.
     * @throws        If an error occurs during the cmd execution a {@link ExecutionException} is thrown.
     */
    Result call(String cmd) throws ExecutionException

    /**
     * Execute the cmd but do not display any output to the Jenkins build console/log.
     *
     * @param cmd     The full command and arguments you want the <code>Shell</code>
     *                to execute.
     * @return        A {@link Result} that contains the execution details.
     * @throws        If an error occurs during the cmd execution a {@link ExecutionException} is thrown.
     */
    Result silent(String cmd) throws ExecutionException

    /**
     * Executes the cmd but does not throw a {@link ExecutionException} if an error
     * occurs. Instead a {@link Result} object is returned with the details of the error.
     *
     * @param cmd     The full command and arguments you want the <code>Shell</code>
     *                to execute.
     * @param silent  If <code>true</code> the results of the execution will not
     *                be shown in the Jenkins build console/log.
     * @return        A {@link Result} that contains the execution details.
     */
    Result ignoreErrors(String cmd, Boolean silent)

}
