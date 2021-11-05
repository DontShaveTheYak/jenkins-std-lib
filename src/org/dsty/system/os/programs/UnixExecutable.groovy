/* groovylint-disable DuplicateNumberLiteral, UnnecessaryGetter */
package org.dsty.system.os.programs

import org.dsty.system.os.Path

/**
 * A {@link Executable} that be run on <code>UNIX</code> systems.
 */
class UnixExecutable extends AbstractExecutable {

    /**
     * The default constructor.
     *
     * @param filePath  The {@link Path} to the executable file.
     */
    protected UnixExecutable(Path filePath) {
        super(filePath)
    }

    /**
     * Checks if the group permission is executable.
     *
     * @return <code>true</code> if the group has execute permission, otherwise <code>false</code>
     */
    Boolean isExecutable() {
        // Need to convert from octal to a string and then to a int
        final String octal = Integer.toOctalString(filePath.mode())
        final int mode = Integer.parseInt(octal)

        // Get the hundredths value
        final int userPermission = (mode / 100).intValue() % 10

        // Checks if the first bit is a 0 or 1
        // all odd numbers start with a 1
        // an odd number 1,3,5 and 7 means execute permissions
        final Boolean isOdd = (userPermission & 1) == 1

        return isOdd
    }

    /**
     * Makes the file executable by giving group permission to execute.
     * <p>
     * This method will check if the file {@link #isExecutable()} first.
     */
    void setExecutePermission() {

        if (isExecutable()) {
            return
        }

        // Get the String octal
        final String octal = Integer.toOctalString(filePath.mode())

        // Convert to int and add 100 to make it executable
        final int newMode = Integer.parseInt(octal) + 100

        // Take the octal string and convert back to int
        final int mode = Integer.parseInt("0${newMode}", 8)

        filePath.chmod(mode)
    }

}
