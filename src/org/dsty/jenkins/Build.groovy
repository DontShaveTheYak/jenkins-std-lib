/* groovylint-disable LineLength, UnnecessaryGetter */
package org.dsty.jenkins

/**
 * Returns the WorkFlowScript from the current build. This object has access
 * to pipeline steps like <code>sh()</code>, <code>checkout()</code> and
 * <code>stage()</code>.
 *
 * @return The current WorkFlowScript.
 */
Object getWorkFlowScript() {
    return this
}

/**
 * Gets the environment variables for the current build. This is the same as using
 * the Pipeline global var <code>env</code>.
 *
 * @return A Map of environment variables.
 */
Map<String, String> environmentVars() {
    return getContext(hudson.EnvVars)
}

/**
 * Returns a contextual object from the current build. This method is shorthand
 * for <code>getWorkFlowScript().getContext(type)</code>.
 *
 * @param type The class of the contextual script like <code>hudson.FilePath</code>.
 * @return The contextual object.
 * @see <a href="https://www.jenkins.io/doc/pipeline/steps/workflow-basic-steps/#getcontext-get-contextual-object-from-internal-apis"target="_blank">workflow-basic-steps/getcontext</a>
 */
Class<?> getCurrentContext(Class<?> type) {
    return getContext(type)
}

return this
