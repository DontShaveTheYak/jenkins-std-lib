/* groovylint-disable UnnecessaryGetter */
package org.dsty.system.os.programs

import org.dsty.system.os.shell.Shell
import org.dsty.system.os.shell.Result
import org.dsty.system.os.shell.ExecutionException

/**
 * A {@link CliTool} is the combination of a {@link org.dsty.system.os.shell.Shell} and a {@link Executable}.
 * <p>
 * This class is just a base class that is capable of running a command as an example.
 * It is expected that this class is extended and additional methods are created to run
 * commands specific to the {@link Executable} you are using.
 */
class CliTool implements Serializable {

    protected Shell shell
    protected Executable bin

    /**
     * Creates a {@link CliTool} from a {@link org.dsty.system.os.shell.Shell} and a {@link Executable}.
     * <p>
     * The {@link Executable} must exist and be executable.
     *
     * @param shell  The {@link org.dsty.system.os.shell.Shell} to use when executing commands.
     * @param bin    The {@link Executable} to use when executing commands.
     */
    CliTool(Shell shell, Executable bin) {
        this.shell = shell
        this.bin = bin
    }

    /**
     * The path to the {@link Executable}.
     * <p>
     * This path is wrapped in single quotes to avoid errors when
     * files or directories have spaces in there names.
     *
     * @return  The path to the {@link Executable}.
     */
    protected String binPath() {

        return "'${bin.getPath()}'"

    }

    /**
     * Runs a command using the path to the {@link Executable} and the given
     * <code>args</code>.
     * <p>
     * The output from this command will be visible in the build console.
     *
     * @param args  The arguments to pass to the {@link Executable}.
     * @return      A {@link org.dsty.system.os.shell.Result shell.Result}.
     */
    protected Result runCommand(String args) {

        return shell.call("${binPath()} ${args}")

    }

    /**
     * Runs a command silently using the path to the {@link Executable} and the given
     * <code>args</code>.
     * <p>
     * The output from this command will not be visible in the build console.
     *
     * @param args  The arguments to pass to the {@link Executable}.
     * @return      A {@link org.dsty.system.os.shell.Result shell.Result}.
     */
    protected Result runCommandSilent(String args) {

        return shell.silent("${binPath()} ${args}")

    }

    /**
     * Runs a command using the path to the {@link Executable} and the given
     * <code>args</code>.
     * <p>
     * If the command fails the {@link org.dsty.system.os.shell.Result shell.Result}
     * is returned instead of throwing {@link ExecutionException}.
     * <p>
     * The output from this command will be visible in the build console but can be turned
     * off by setting <code>silent</code> to <code>true</code>.
     *
     * @param args    The arguments to pass to the {@link Executable}.
     * @param silent  Set to <code>true</code> to run the command silently, defaults to
                      <code>false</code>.
     * @return        A {@link org.dsty.system.os.shell.Result shell.Result}.
     */
    protected Result runCommandIgnoreErrors(String args, Boolean silent = false) {

        return shell.ignoreErrors("${binPath()} ${args}", silent)

    }

}
