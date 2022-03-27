/* groovylint-disable UnnecessaryGetter */
package org.dsty.system.os.shell

import org.dsty.logging.LogClient
import org.dsty.jenkins.Build
import org.dsty.system.os.Path

/**
 * A {@link Shell} for executing commands on <code>UNIX</code>
 * using the <code>Bash</code> executable.
 * <p>
 * The underlying implementation is actually the Jenkins
 * <a href="https://www.jenkins.io/doc/pipeline/steps/workflow-durable-task-step/#sh-shell-script"
 * target="_blank">sh()</a> step.
 */
class Bash implements Shell {

    /**
     * If <code>true</code> then the script is stopped on first non-zero exit code.
     */
    Boolean failFast = true

    /**
     * Workflow script representing the jenkins build.
     */
    private Object wfs

    /**
     * Logging client
     */
    private LogClient log



    /**
     * Formats a bash script by adding the shebang, setting the verbose level and sourcing bashrc.
     *
     * @param userScript  The bash script you want to run.
     * @param silent      If <code>true</code> the results of the execution will not
     *                    be shown in the Jenkins build console/log.
     * @return            A bash script that is ready to be executed.
     */
    String formatScript(final String userScript, final Boolean silent=false) {
        final String showOutput = 'exec 2> >(tee -a stderr stdall) 1> >(tee -a stdout stdall)'
        final String hideOutput = 'exec 3>/dev/null 2> >(tee -a stderr stdall >&3) 1> >(tee -a stdout stdall >&3)'
        final String exec = silent ? hideOutput : showOutput
        final String setE = this.failFast ? 'set -e;' : 'set +e;'
        final String setX = this.wfs.env.PIPELINE_LOG_LEVEL == 'DEBUG' ? 'set -x;' : 'set +x;'

        final String header = """\
            #!/bin/bash
            source \$HOME/.bashrc > /dev/null 2>&1 || true
            { ${setE} } > /dev/null 2>&1
            ${exec}
            { ${setX} } > /dev/null 2>&1

            # User Script
        """.stripIndent()

        final String script = "${header}\n${userScript.stripIndent()}"
        this.log.debug("Formatted script:\n${script}")

        return script
    }

    /**
     * Execute the command or script using <code>bash</code>.
     *
     * @param userScript  The bash command or script you want to execute.
     * @param silent      If <code>true</code> the results of the execution will not
     *                    be shown in the Jenkins build console/log.
     * @return            A {@link Result} that contains the execution details.
     * @throws            If an error occurs during the cmd execution a {@link ExecutionException} is thrown.
     */
    Result call(final String userScript) throws ExecutionException {
        final String script = formatScript(userScript, false)

        return execute(script)
    }

    /**
     * Execute the command or script using <code>bash</code> but do not display any
     * output to the Jenkins build console/log.
     *
     * @param userScript  The bash command or script you want to execute.
     * @param silent      If <code>true</code> the results of the execution will not
     *                    be shown in the Jenkins build console/log.
     * @return            A {@link Result} that contains the execution details.
     * @throws            If an error occurs during the cmd execution a {@link ExecutionException} is thrown.
     */
    Result silent(final String userScript) throws ExecutionException {
        final String script = formatScript(userScript, true)

        return execute(script)
    }

    /**
     * Executes the command or script but does not throw a {@link ExecutionException} if an error
     * occurs. Instead a {@link Result} object is returned with the details of the error.
     *
     * @param userScript  The bash command or script you want to execute.
     * @param silent      If <code>true</code> the results of the execution will not
     *                    be shown in the Jenkins build console/log.
     * @return            A {@link Result} that contains the execution details.
     */
    Result ignoreErrors(String userScript, Boolean silent=false) {
        String script = formatScript(userScript, silent)
        Result result

        try {
            result = execute(script)
        } catch (ExecutionException ex) {
            result = new Result(ex.stdOut, ex.stdErr, ex.output, ex.exitCode)
        }

        return result
    }

    /**
     * Executes the script using the Jenkins <code>sh()</code> step.
     *
     * @param script  The bash command or script you want to run.
     * @return        The results of the bash command or script.
     * @throws        If an error occurs during the cmd execution a {@link ExecutionException} is thrown.
     */
    private Result execute(String script) throws ExecutionException {
        final Integer exitCode = this.wfs.sh(script: script, returnStatus: true)

        def(String stdOut, String stdErr, String output) = readOutputs()

        if (exitCode) {
            throw new ExecutionException(stdOut, stdErr, output, exitCode)
        }

        return new Result(stdOut, stdErr, output, exitCode)
    }

    /**
     * Reads the script outputs from files and then removes them.
     * @return The stdout, stderr and combined script output.
     */
    private List<String> readOutputs() {

        final List<String> fileNames = ['stdout', 'stderr', 'stdall']

        final Path cwd = Path.cwd()

        final List<Path> files = fileNames.collect { String fileName ->
            cwd.child(fileName)
        }

        final List<String> outputs = files.collect { Path filePath ->
            filePath.read().replaceAll('(?m)^\\+.*(?:\r?\n)?', '').trim()
        }

        files.each { Path filePath ->
            filePath.delete()
        }

        return outputs
    }

    private Object getWfs() {
        setWfs()
        return this.@wfs
    }

    private void setWfs() {
        Build build = new Build()
        this.wfs = build.getWorkFlowScript()
    }

    private Object getLog() {
        setLog()
        return this.@log
    }

    private void setLog() {
        this.log = new LogClient()
    }

}
