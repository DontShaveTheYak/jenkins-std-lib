/* groovylint-disable DuplicateNumberLiteral, UnnecessaryGetter */
package org.dsty.system.os.programs

import org.dsty.system.os.Path

/**
 * The base class for all {@link Executable Executables}.
 */
abstract class AbstractExecutable implements Serializable, Executable {

    /**
     * The path to the executable file.
     */
    final protected Path filePath

    /**
     * The default constructor.
     *
     * @param filePath The path to the executable on the disk.
     */
    protected AbstractExecutable(Path filePath) {
        this.filePath = filePath
    }

    /**
     * Gets the absolute path to the executable.
     */
    String getPath() {
        return filePath.absolutize()
    }

    /**
     * Checks if the executable can be executed on the current platform.
     *
     * @return <code>true</code> if the file can be executed on the current platform,
               otherwise <code>false</code>.
     */
    abstract Boolean isExecutable()

    /**
     * Make the file executable.
     * <p>
     * On <code>UNIX</code> this means making sure the user has execute permissions and
     * on <code>WINDOWS</code> this means making sure the file has the correct extension.
     */
    abstract void setExecutePermission()

}
