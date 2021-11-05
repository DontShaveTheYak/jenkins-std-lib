/* groovylint-disable UnnecessaryGetter */
package org.dsty.system.os.programs

import org.dsty.system.os.Path
import org.dsty.system.os.shell.Shell

/**
 * A {@link ToolInstaller} that uses an install script to install
 * a {@link CliTool} to the system.
 */
abstract class ScriptInstaller extends UrlInstaller {

    /**
     * This is the path to the install script returned by the
     * {@link org.dsty.system.os.download.FileRetriever FileRetriever}.
     * <p>
     * When installing a executable directly, then this is typically just the
     * name of the file. When installing from a zip file, where the executable
     * is inside a subdir called <code>my_custom_bin</code> then {@link #getScriptPath()}
     * should return <code>"my_custom_bin/my_script_name"</code>.
     * <p>
     * By default this method returns <code>"${getBinName()}_installer"</code> but should be
     * overriden if {@link #downloadUrl(String, String) downloadUrl} will point to a zip/tar file.
     *
     * @return  The path to the install script, relative to the {@link #installDir()}.
     */
    String getScriptPath() {
        return "${getBinName()}_installer"
    }

    /**
     * The arguments to pass to the install script.
     * <p>
     * For the install to work correctly the install script must take an argument to choose
     * the install directory and you must pass the {@link #installDir()} to it.
     *
     * @return  The arguments to pass to the install script located at {@link #getScriptPath()}.
     */
    abstract String getScriptArgs()

    /**
     * Installs the {@link CliTool} to the system.
     * <p>
     * When called this will first check if the tool is installed using {@link #isInstalled()}
     * and if not then it will call {@link #download()} to get the install script from the
     * {@link #downloadUrl()} using the {@link org.dsty.system.os.download.FileRetriever FileRetriever}
     * specified by {@link #getRetriever()}. It will then execute the install script with the
     * args from {@link #getScriptArgs()}.
     * <p>
     * The {@link CliTool} is then created using {@link #getTool()}.
     *
     * @return A {@link CliTool} that is ready to run commands on the system.
     */
    CliTool install() {

        if (!isInstalled()) {

            Path script = isArchive() ? download() : download(getScriptPath())

            Executable installer = ExecutableBuilder.buildExecutable(script)

            Shell shell = getShell()

            shell.call("${installer.getPath()} ${getScriptArgs()}")
        }

        return getTool()
    }

}
