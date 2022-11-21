/* groovylint-disable ThrowException */
package org.dsty.system.os.programs

import java.nio.file.NoSuchFileException

import org.dsty.system.os.Path
import org.dsty.system.Platform
import org.dsty.system.System
import org.dsty.system.os.programs.UnixExecutable

/**
 * Creates {@link Executable Executables} for the current system.
 */
class ExecutableBuilder {

    /**
     * Returns an {@link Executable} for the current system.
     * <p>
     * Currently only <code>UNIX</code> is supported. If method
     * is called on any other system then <code>UnsupportedSystemException</code>
     * exception is thrown.
     *
     * @throws  NoSuchFileException If the <code>filePath</code> does not exist.
     * @param   filePath The {@link Path} to the executable file.
     * @return  A {@link Executable} that has the correct execution permissions.
     */
    static Executable buildExecutable(Path filePath) throws NoSuchFileException {

        if (!filePath.exists()) {
            throw new NoSuchFileException(filePath.absolutize().toString())
        }

        final System os = Platform.system()

        if (os.name() != 'UNIX') {
            throw new Exception('UnsupportedSystemException')
        }

        Executable bin = new UnixExecutable(filePath)

        bin.setExecutePermission()

        return bin
    }

}
