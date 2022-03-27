package org.dsty.scm

import org.dsty.logging.LogClient

/**
 * Generic SCM client capable of checking out code.
 */
class Generic implements Serializable {

    /**
     * Workflow script representing the jenkins build.
     */
    protected final Object steps

    /**
     * Logging client
     */
    protected final LogClient log

    /**
     * Default Constructor
     * <pre>{@code
     * import org.dsty.scm.Generic
     *node() {
     *  Generic scmClient = new Generic(this)
     *  Map options = [
     *      changelog: false,
     *      poll: false,
     *      scm: [
     *          $class: 'GitSCM',
     *          branches: [[name: 'master']],
     *          extensions: [],
     *          userRemoteConfigs: [[url: 'https://github.com/cplee/github-actions-demo.git']]
     *      ]
     *  ]
     *  scmClient.checkout(options)
     *&#125;}</pre>
     * @param steps The workflow script representing the jenkins build.
     */
    Generic(Object steps) {
        this.steps = steps
        this.log = new LogClient()
    }

    /**
     * Behaves like the native Jenkins checkout step but this one
     * saves the GIT_* envioronment variables correctly.
     * @param options The valid keys and values can be found
     * <a href="https://www.jenkins.io/doc/pipeline/steps/workflow-scm-step/">here</a>.
     * @return The outputs from the checkout.
     */
    Map checkout(Map options) {
        Map results

        results = this.executeCheckout(options)

        this.saveEnvironment(results)

        return results
    }

    /**
     * Same as {@link #checkout()} but for MultiBranch pipelines.
     * @param options The valid keys and values can be found
     * <a href="https://www.jenkins.io/doc/pipeline/steps/workflow-scm-step/">here</a>.
     * @return The outputs from the checkout.
     */
    Map checkoutMultiBranch() {
        Map results

        results = this.executeCheckout(this.steps.scm)

        this.saveEnvironment(results)

        return results
    }

    /**
     * Same as {@link #checkout()} but this will restore the original GIT_*
     * environment variables before it returns.
     * @param options The valid keys and values can be found
     * <a href="https://www.jenkins.io/doc/pipeline/steps/workflow-scm-step/">here</a>.
     * @param userCode the steps you want to take while the code is checked out.
     * @return The outputs from the checkout.
     */
    Map withCheckout(Map options, Closure userCode) {
        Map results

        results = this.executeCheckout(options)

        List envVars = results.collect { k, v ->
            "${k}=${v}"
        }

        this.steps.withEnv(envVars) {
            userCode()
        }

        return results
    }

    /**
     * Runs the native Jenkins checkout step.
     * @param options The valid keys and values can be found
     * <a href="https://www.jenkins.io/doc/pipeline/steps/workflow-scm-step/">here</a>.
     * @return The outputs from the checkout.
     */
    protected Map executeCheckout(Object options) {
        Map results = this.steps.checkout(options)

        this.log.debug("Checkout information:\n${this.log.pprint(results)}")
        return results
    }

    /**
     * Save the outputs from the checkout step as environment variables.
     * @param envVars The values you want to save as environment variables.
     */
    protected void saveEnvironment(Map envVars) {
        envVars.each { k, v -> this.steps.env.setProperty(k, v) }
    }

}
