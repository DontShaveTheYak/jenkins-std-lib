package org.dsty.system.os.programs

/**
 * A ToolInstaller installs {@link CliTool CliTool's} into the workspace from
 * a variety of sources.
 */
interface ToolInstaller {

    /**
     * Installs a {@link CliTool} into the current build's workspace in the <code>.tools</code>
     * directory. The install location can be changed to enable tool caching using the
     * environment variable <code>JSL_TOOLS_DIR</code>.
     *
     * @return A {@link CliTool} that is ready to run commands.
     * @see org.dsty.jenkins.Instance#toolsDir()
     */
    CliTool install()

}
