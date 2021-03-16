package org.dsty.jenkins

import com.cloudbees.groovy.cps.NonCPS
import jenkins.model.Jenkins

/**
 * This class consists exclusively of static methods that operate on
 * or returns information about the current Jenkins server.
 */
class Instance implements Serializable {

  /**
    * Checks if a plugin is installed.
    * @param shortName The name of the plugin.
    * @return True if the plugin is installed.
    */
  @NonCPS
  static Boolean pluginInstalled(String shortName) {
    List plugins = plugins()

    String plugin = plugins.find { it == shortName }

    return plugin as Boolean
  }

  /**
    * Returns the plugins currently installed on the
    * Jenkins. This does not check if a plugin is enabled
    * or active in the current build.
    * @return List of plugin shortNames/ID.
    */
  @NonCPS
  static List<String> plugins() {
    return Jenkins.instance.pluginManager.plugins*.getShortName()
  }

}
