/* groovylint-disable UnnecessaryGetter */
package org.dsty.jenkins

import com.cloudbees.groovy.cps.NonCPS
import jenkins.model.Jenkins

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

}
