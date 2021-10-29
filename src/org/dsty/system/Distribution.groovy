/* groovylint-disable ThrowException, UnnecessaryGetter */
package org.dsty.system

import org.dsty.system.os.shell.Shell
import org.dsty.system.os.shell.Result

/**
 * Provides information about the version of the current {@link System}.
 */
class Distribution {

    /**
     * Returns the name of the current {@link System System's} distribution.
     * <p>
     * Currently only {@link System#UNIX} is supported. The distribution is determined by
     * <code>lsb_release -i</code>. If the {@link System} doesn't have <code>lsb_release</code>
     * installed than <code>UNKNOWN</code> is returned.
     *
     * @param   system The current {@link System}.
     * @return  The name of the current distribution or <code>UNKNOWN</code>.
     */
    static String name(System system) {

        String dist

        switch (system) {
            case system.UNIX:
                dist = unixDist(system)
                break
            default:
                throw new Exception("Distribution name is not supported for ${system}.")
        }

        return dist
    }

    /**
     * Get the distribution name for <code>UNIX</code> {@link System Systems}.
     * <p>
     * On <code>UNIX</code> {@link System Systems} this value comes from
     * <code>lsb_release -i</code>.
     *
     * @param   system The current {@link System}.
     * @return  The name of the current <code>UNIX</code> distribution or <code>UNKNOWN</code>.
     */
    private static String unixDist(System system) {

        String distName = 'UNKNOWN'

        Shell shell = system.getShell()

        Result result = shell.ignoreErrors('lsb_release -i', true)

        if (!result.exitCode) {
            distName = result.stdOut.split()[-1]
        }

        return distName
    }

}
