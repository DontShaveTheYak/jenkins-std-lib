/* groovylint-disable ThrowException, UnnecessaryGetter */
package org.dsty.system

import org.dsty.system.os.shell.Bash
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
     * Returns the {@link System} for the current agent.
     *
     * @return The {@link System}.
     */
    static System system() {

        Build build = new Build()

        hudson.Platform platform = build.getEnvironment().getPlatform()

        String currentPlatform = platform

        if (platform == hudson.Platform.UNIX && platform.isDarwin()) {
            currentPlatform = 'DARWIN'
        }

        return (currentPlatform as System)
    }

    /**
     * The architecture value that is returned by the
     * <code>os.arch</code> java property.
     *
     * @return The architecture type.
     */
    static String architecture() {

        final System currentSystem = system()

        return currentSystem.architecture()
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

        System currentSystem = system()

        return currentSystem.getShell()
    }

}
