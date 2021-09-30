/* groovylint-disable ThrowException, UnnecessaryGetter */
package org.dsty.system

import org.dsty.system.os.shell.Bash
import org.dsty.system.os.shell.Result
import org.dsty.system.os.shell.Shell
import org.dsty.jenkins.Build

/**
 * Provides information about the hardware and os the current
 * build is running on.
 */
class Platform implements Serializable {

    /**
     * Sets the default {@link Shell} for the <code>UNIX</code> system.
     */
    static Class<Shell> unixShell = Bash

    /**
     * Sets the default {@link Shell} for the <code>WINDOWS</code> system.
     */
    static Class<Shell> winShell

    /**
     * Sets the default {@link Shell} for the <code>DARWIN</code> system.
     */
    static Class<Shell> darwinShell

    /**
     * Returns the system/OS name. The returned value will be one of <code>UNIX</code>,
     * <code>DARWIN</code> or <code>WINDOWS</code>.
     *
     * @return The type of system the current build is running on.
     */
    static String system() {

        Build build = new Build()

        hudson.Platform platform = build.getEnvironment().getPlatform()

        String currentPlatform = platform

        if (platform == hudson.Platform.UNIX && platform.isDarwin()) {
            currentPlatform = 'DARWIN'
        }

        return currentPlatform
    }

    /**
     * Returns the architecture that is returned from
     * <code>uname -m</code>.
     *
     * @return The architecture type.
     * @see https://en.wikipedia.org/wiki/Uname
     */
    static String architecture() {

        Shell shell = getShell()

        Result result = shell.silent('uname -m')

        return result.stdOut.trim()
    }

    /**
     * Return a shell for the current {@link #system} type.
     * <p>
     * The default shell for each system can be set by modifying the fields
     * {@link #unixShell}, {@link #winShell} and {@link #darwinShell}.
     *
     * @return A {@link Shell}.
     */
    static Shell getShell() {

        String platform = system()

        Map<String, Class<Shell>> shells = [
            'UNIX': unixShell,
            'WINDOWS': winShell,
            'DARWIN': darwinShell
        ]

        Class<Shell> platformShell = shells[platform]

        if (!platformShell) {
            throw new Exception('UnsupportedPlatform')
        }

        return platformShell.newInstance()
    }

}
