/* groovylint-disable UnnecessaryPackageReference */
package org.dsty.system

import org.dsty.system.os.shell.Shell

/**
 * The type of agent the current build is running on.
 */
enum System {

    /**
     * A system derived from <code>UNIX</code>.
     */
    UNIX,

    /**
     * A system made by Microsoft.
     */
    WINDOWS,

    /**
     * A system made by Apple.
     */
    DARWIN

    /**
     * Gets the current {@link org.dsty.system.os.shell.Shell} for the {@link System}.
     * <p>
     * Most {@link System Systems} can have more than one {@link org.dsty.system.os.shell.Shell}.
     * The default shell is determine by {@link Platform#unixShell}, {@link Platform#winShell}
     * and {@link Platform#darwinShell}.
     *
     * @return  The {@link org.dsty.system.os.shell.Shell} for the current {@link System}.
     */
    Shell getShell() {

        Class<Shell> shell

        switch (this) {
            case UNIX:
                shell = Platform.unixShell
                break
            case WINDOWS:
                shell = Platform.winShell
                break
            case DARWIN:
                shell = Platform.darwinShell
                break
        }

        return shell.newInstance()
    }

    /**
     * The name of the current {@link System System's} OS.
     * <p>
     * For {@link #UNIX} this might be <code>Ubuntu</code> or <code>CentOS</code>.
     *
     * @return  The name of the OS.
     */
    String distribution() {
        return Distribution.name(this)
    }

    /**
     * The architecture value that is returned by the
     * <code>os.arch</code> java property.
     *
     * @return The architecture type.
     */
    String architecture() {
        return java.lang.System.getProperty('os.arch')
    }

}
