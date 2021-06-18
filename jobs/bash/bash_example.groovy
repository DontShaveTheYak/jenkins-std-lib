/* groovylint-disable DuplicateNumberLiteral, DuplicateStringLiteral */
@Library('jenkins-std-lib')
import org.dsty.bash.BashClient
import org.dsty.bash.ScriptError
import org.dsty.bash.Result

node() {
    String msg
    Result result
    BashClient bash = new BashClient(this)

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
        ScriptError exception = null

        try {
            bash('RegularFakeCommand')
        } catch (ScriptError e) {
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
        result = bash.ignoreErrors('fakecommand', true)

        if ( !result.stdErr.contains('fakecommand: command not found') ) {
            error('Command was found.')
        }

        if (result.stdOut) {
            error('Should not have stdOut.')
        }

        if ( result.exitCode != 127) {
            error('Exited with wrong code.')
        }

        // By default ignoreErrors will ignore all errors and run
        // the entire script before returning.

        String script = '''\
        fakeCommand
        anotherFakeCommand
        '''

        // The false we are passing determines if the command output is to be
        // displayed in the build console.
        result = bash.ignoreErrors(script, false)

        if ( !result.stdErr.contains('anotherFakeCommand') ) {
            error('Should not stop on first error.')
        }

        // If you want to return on the first error,
        // set failfast to true.
        result = bash.ignoreErrors(script, false, true)

        if ( result.stdErr.contains('anotherFakeCommand') ) {
            error('Should stop on first error.')
        }

        result = bash.ignoreErrors('exit 55', false)

        if ( result.exitCode != 55 ) {
            error('Should capture the correct exitCode.')
        }
    }
}
