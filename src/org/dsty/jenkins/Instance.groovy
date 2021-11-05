/* groovylint-disable UnnecessaryGetter */
package org.dsty.jenkins

import com.cloudbees.groovy.cps.NonCPS
import jenkins.model.Jenkins
import org.dsty.system.os.Path

/**
 * This class represents the current Jenkins instance. It can be used to
 * disover information like what plugins are are currently installed.
 */
class Instance implements Serializable {

    /**
     * Checks if a plugin is installed.
     *
     * @param shortName The name/ID of the plugin.
     * @return True if the plugin is installed.
     */
    @NonCPS
    static Boolean pluginInstalled(String shortName) {
        List plugins = plugins()

        String plugin = plugins.find { pluginName -> pluginName == shortName }

        return plugin as Boolean
    }

    /**
     * Returns the plugins currently installed on the
     * Jenkins. This does not check if a plugin is enabled
     * or active in the current build.
     *
     * @return List of plugin shortNames/ID.
     */
    @NonCPS
    static List<String> plugins() {
        return Jenkins.instance.pluginManager.plugins*.getShortName()
    }

    /**
     * Gets the {@link org.dsty.system.os.Path Path} to the directory where build tools are installed.
     * <p>
     * The directory can be configured with the environment variable <code>JSL_TOOLS_DIR</code>.
     * This is useful to set the tool directory to someplace outside of the build workspace so that
     * tools are cached between builds. This can also be set to a directory shared with multiple agents
     * using a shared filesystem. This allows tools to be cached across multiple agents.
     * <p>
     * If the <code>JSL_TOOLS_DIR</code> envrionment variable is not set to a value then this
     * method returns <code>null</code>.
     *
     * @return  The {@link org.dsty.system.os.Path Path} to the tool installation directory.
     */
    static Path toolsDir() {
        Path toolDir

        Map envVars = new Build().environmentVars()

        toolDir = envVars.JSL_TOOLS_DIR ? Path(envVars.JSL_TOOLS_DIR) : null

        return toolDir
    }

}
