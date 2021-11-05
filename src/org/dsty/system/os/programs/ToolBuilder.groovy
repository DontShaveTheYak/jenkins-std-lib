/* groovylint-disable DuplicateStringLiteral, ThrowException, UnnecessaryGetter */
package org.dsty.system.os.programs

import java.lang.reflect.Constructor

import org.dsty.system.os.Path
import org.dsty.system.Platform
import org.dsty.system.System
import org.dsty.system.os.shell.Shell

/**
 * Creates {@link CliTool CliTool's} for the current system.
 */
class ToolBuilder {

    /**
     * Returns an executable for the current system.
     * <p>
     * Currently only <code>UNIX</code> is supported. If method
     * is called on any other system then <code>UnsupportedSystemException</code>
     * exception is thrown.
     */
    static CliTool buildTool(Path filePath) {
        final System os = Platform.system()

        if (os.name() != 'UNIX') {
            throw new Exception('UnsupportedSystemException')
        }

        Executable bin = ExecutableBuilder.buildExecutable(filePath)

        return buildTool(bin)
    }

    /**
     * Returns an executable for the current system.
     * <p>
     * Currently only <code>UNIX</code> is supported. If method
     * is called on any other system then <code>UnsupportedSystemException</code>
     * exception is thrown.
     */
    static CliTool buildTool(Executable executable) {
        final System os = Platform.system()

        if (os.name() != 'UNIX') {
            throw new Exception('UnsupportedSystemException')
        }

        Shell shell = Platform.getShell()

        return new CliTool(shell, executable)
    }

    static CliTool buildTool(ToolInstaller installer) {

        Executable executable = ExecutableBuilder.buildExecutable(installer.installPath())

        Class<CliTool> toolClass = installer.useTool()

        Constructor toolCon = toolClass.getConstructor(Shell, Executable)

        CliTool tool = toolCon.newInstance(installer.getShell(), executable)

        return tool

    }

}
