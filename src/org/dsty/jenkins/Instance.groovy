package org.dsty.jenkins

import com.cloudbees.groovy.cps.NonCPS
import jenkins.model.Jenkins

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
  * @return The userScript formatted for bash.
  */
@NonCPS
static List<String> plugins() {
  return Jenkins.instance.pluginManager.plugins*.getShortName()
}

return this
