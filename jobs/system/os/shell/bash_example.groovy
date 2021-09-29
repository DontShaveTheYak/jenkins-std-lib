/* groovylint-disable DuplicateNumberLiteral, DuplicateStringLiteral */
@Library('jenkins-std-lib')
import org.dsty.system.os.shell.Bash
import org.dsty.system.os.shell.ExecutionException
import org.dsty.system.os.shell.Result

node() {
    String msg
    Result result
    Bash bash = new Bash()

    stage('Regular Bash') {
        msg = 'Hello from Bash!'

        // bash() prints the output to the console like sh,
        // but it also returns the stdOut, stdError, and exitCode.
        // It also returns output which is stdOut and stdError combined
        // but the order is not guranteed to be perfect.
        result = bash("echo '${msg}'")

        if (result.stdOut != msg ) {
            error('Did not contain correct output.')
        }

        if ( result.stdErr ) {
            error('Should not have output anything.')
        }

        if ( result.exitCode != 0 ) {
            error('Exited with wrong code.')
        }

        // In the event of a non 0 exitCode an Exception
        // is thrown. The exception will behave just like a normal Result.
        // So it will also have stdOut, stdError, output and exitCode.
        ExecutionException exception = null

        try {
            bash('RegularFakeCommand')
        } catch (ExecutionException e) {
            exception = e
        }

        if ( !exception.stdErr.contains('RegularFakeCommand: command not found') ) {
            error('Command was found.')
        }

        if (exception.stdOut) {
            error('Should not have stdOut.')
        }

        if ( exception.exitCode != 127) {
            error('Exited with wrong code.')
        }
    }

    stage('Silent Bash') {
        msg = 'NotShown!'

        result = bash.silent("echo '${msg}'")

        if (result.stdOut != msg ) {
            error('Did not contain correct output.')
        }

        if ( result.stdErr ) {
            error('Should not have output anything.')
        }

        if ( result.exitCode != 0 ) {
            error('Exited with wrong code.')
        }
    }

    stage('Ignore Errors') {
        // ignoreErrors will not throw an exception.
        // Instead is just returns the results after
        // it encounters an error.
        result = bash.ignoreErrors('fakecommand')

        if ( !result.stdErr.contains('fakecommand: command not found') ) {
            error('Command was found.')
        }

        if (result.stdOut) {
            error('Should not have stdOut.')
        }

        if ( result.exitCode != 127) {
            error('Exited with wrong code.')
        }

        String script = '''\
        fakeCommand
        willNotRun
        '''

        // The true we are passing determines if the command
        // is run silently or not.
        result = bash.ignoreErrors(script, true)

        // By default all bash commands will stop on
        // the first error.
        if ( result.stdErr.contains('willNotRun') ) {
            error('Should have stopped on first error.')
        }

        script = """\
        moreFakeCommands
        commandWillRun
        """

        // If you want to run all the commands, regardless of errors,
        // Set failFast to false
        bash.failFast = false
        result = bash.ignoreErrors(script)

        if ( !result.stdErr.contains('commandWillRun') ) {
            error('Should not stop on first error.')
        }

        result = bash.ignoreErrors('exit 55', true)

        if ( result.exitCode != 55 ) {
            error('Should capture the correct exitCode.')
        }
    }
}
