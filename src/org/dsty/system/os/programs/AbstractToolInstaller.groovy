/* groovylint-disable UnnecessaryGetter */
package org.dsty.system.os.programs

import org.dsty.jenkins.Instance

import org.dsty.system.os.Path
import org.dsty.system.Platform
import org.dsty.system.System
import org.dsty.system.os.programs.ToolBuilder
import org.dsty.system.os.shell.Shell

/**
 * The base class for all {@link ToolInstaller ToolInstaller's} that install {@link CliTool CliTool's} to the system.
 */
abstract class AbstractToolInstaller implements Serializable, ToolInstaller {

    /**
     * Return the name of the binary, which is typically the name of the tool.
     * <p>
     * For a tool like Terraform the name would be <code>terraform</code>. This name
     * is used to identify this tool when logging to the console. It's also used in
     * the installation path.
     *
     * @see     #installDir()
     * @return  The name of the binary which is often the tool name.
     */
    abstract String getBinName()

    /**
     * This is the path to the executable inside of it's installation medium.
     * <p>
     * When installing a executable directly, then this is typically just the
     * {@link #getBinName()}. When installing from a zip file, where the executable
     * is inside a subdir called <code>my_custom_bin</code> then {@link #getBinPath()}
     * should return <code>"my_custom_bin/${getBinName()}"</code>.
     *
     * @see     #installPath()
     * @return  The path to the executable, relative to the {@link #installDir()}
     */
    abstract String getBinPath()

    /**
     * The version of the executable that is being installed.
     * <p>
     * The version is used in the installation path. If the executable does not have
     * a version then return something like <code>0.0.0</code> or <code>latest</code>.
     *
     * @see     #installPath()
     * @return  An identifer marking the version of the executable.
     */
    abstract String getVersion()

    /**
     * This method when called will install the executable to the system and then return a
     * {@link CliTool} that is ready to execute commands.
     * <p>
     * It's recommened that you check if the executable is already installed first by using {@link #isInstalled()}
     * first.
     *
     * @see     #getTool()
     * @return  A {@link CliTool} that is ready to execute commands.
     */
    abstract CliTool install()

    /**
     * Checks to see if the executable is already installed on the system.
     *
     * @see     #installPath()
     * @return  <code>true</code> if the executable is already present, otherwise <code>false</code>.
     */
    Boolean isInstalled() {

        Path tool = installPath()

        return tool.exists()
    }

    /**
     * Returns the default {@link org.dsty.system.os.shell.Shell} for the given platform.
     * <p>
     * This method should be overidden if the {@link CliTool} you are implementing is only applicable to
     * a certain platform, like <code>WINDOWS</code>.
     *
     * @see     org.dsty.system.Platform#getShell()
     * @return  The {@link org.dsty.system.os.shell.Shell} that will be used when creating the {@link CliTool}.
     */
    protected Shell getShell() {
        return Platform.getShell()
    }

    /**
     * Sets the type of tool to use.
     * <p>
     * You will most likely want to override this method to a custom implementation.
     *
     * @return  The class {@link CliTool}.
     */
    protected Class<CliTool> useTool() {
        return CliTool
    }

    /**
     * Gets the {@link CliTool} from the {@link ToolBuilder}.
     * <p>
     * This should be called at the end of {@link #install()}.
     *
     * @see     #install()
     * @return  A {@link CliTool} that has been {@link #install() installed} and ready to use.
     */
    protected CliTool getTool() {
        return ToolBuilder.buildTool(this)
    }

    /**
     * Get the current {@link org.dsty.system.System}.
     *
     * @return  The {@link org.dsty.system.System} instance.
     */
    protected System getSystem() {

        return Platform.system()

    }

    /**
     * The {@link org.dsty.system.os.Path Path} to the installed {@link Executable}.
     * <p>
     * The {@link org.dsty.system.os.Path Path} is created by combining the output from
     * {@link #installDir()} and {@link #getBinPath()}.
     *
     * @return  The {@link org.dsty.system.os.Path Path} to the installed {@link Executable}.
     */
    protected Path installPath() {

        return installDir().child(getBinPath())
    }

    /**
     * The {@link org.dsty.system.os.Path Path} to the directory where the {@link Executable} will
     * be installed.
     * <p>
     * The {@link org.dsty.system.os.Path Path} is created by getting the {@link #toolsDir()} and then
     * appending the {@link #getBinName()} and the {@link #getVersion()}.
     *
     * @return  The {@link org.dsty.system.os.Path Path} to the directory where the {@link Executable} will
     *          be installed.
     */
    protected Path installDir() {

        Path toolDir = toolsDir().child("${getBinName()}/${getVersion()}")
        toolDir.mkdirs()

        return toolDir
    }

    /**
     * The {@link org.dsty.system.os.Path Path} to the tool installation directory.
     * <p>
     * By default this is the <code>.tools</code> directory inside of the builds workspace
     * but can also be set to a specific directory using the <code>JSL_TOOLS_DIR</code>
     * environment variable.
     *
     * @see     org.dsty.jenkins.Instance#toolsDir()
     * @return  The {@link org.dsty.system.os.Path Path} to the tool installation directory.
     */
    protected Path toolsDir() {

        Path installDir = Instance.toolsDir()

        if (!installDir) {
            installDir = Path.workspace().child('.tools')
            installDir.mkdirs()
        }

        return installDir
    }

}
