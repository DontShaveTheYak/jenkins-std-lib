package org.dsty.system.os.programs

/**
 * A file that can be executed using a {@link org.dsty.system.os.shell.Shell}.
 */
interface Executable {

    /**
     * Checks if the executable can be executed on the current platform.
     *
     * @return <code>true</code> if the file can be executed on the current platform,
               otherwise <code>false</code>.
     */
    Boolean isExecutable()

    /**
     * Make the file executable.
     * <p>
     * On <code>UNIX</code> this means making sure the user has execute permissions and
     * on <code>WINDOWS</code> this means making sure the file has the correct extension.
     */
    void setExecutePermission()

    /**
     * Gets the absolute path to the executable.
     */
    String getPath()

}
