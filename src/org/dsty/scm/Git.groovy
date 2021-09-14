/* groovylint-disable CatchException, DuplicateMapLiteral, DuplicateStringLiteral, ThrowException */
package org.dsty.scm

import org.dsty.bash.Result
import org.dsty.bash.BashClient
import org.dsty.bash.ScriptError

/**
 * Git client for interacting with Git repositories.
 */
class Git extends Generic implements Serializable {

    /**
     * Bash client
     */
    private final BashClient bash

    /**
     * Default Constructor
     * <pre>{@code
     * import org.dsty.scm.Git
     *node() {
     *  Git gitClient = new Git(this)
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
     *  gitClient.checkout(options)
     *&#125;}</pre>
     * @param steps The workflow script representing the jenkins build.
     */
    Git(Object steps) {
        super(steps)
        this.bash = new BashClient(steps)
    }

    /**
     * Change to a branch in the current repo.
     * @param branchName The name of the git branch.
     */
    void changeBranch(String branchName) {
        this.executeGit("checkout ${branchName}")
        this.steps.env.GIT_BRANCH = branchName
    }

    /**
     * Same as {@link #changeBranch()} but this will restore the original
     * branch before it returns.
     * @param branchName The name of the git branch.
     * @param userCode the steps you want to take while the branch is checked out.
     */
    void withBranch(String branchName, Closure userCode) {
        String previousBranch = this.steps.env.GIT_BRANCH

        this.changeBranch(branchName)

        try {
            userCode()
        } catch (Exception ex) {
            this.changeBranch(previousBranch)
            throw ex
        }

        this.changeBranch(previousBranch)
    }

    /**
     * Get a list of branch names that are avaliable locally.
     * @return The name of the branches.
     */
    List<String> localBranches() {
        Result result = this.executeGit('for-each-ref refs/heads --format "%(refname:strip=2)" | paste -sd "," -')

        List<String> branches = result.stdOut.trim().split(',')
        return branches
    }


    /**
     * Get a list of branch names that are avaliable on the remote.
     * @return The name of the branches.
     */
    List<String> remoteBranches() {
        Result result = this.executeGit('branch -r | cut -c 3- | paste -sd "," -')

        List<String> branches = result.stdOut.trim().split(',')
        return branches
    }

    /**
     * Get a list of all tags.
     * @return The name of the tags.
     */
    List<String> tags() {
        Result result = this.executeGit('tag | paste -sd "," -')

        List<String> branches = result.stdOut.trim().split(',')
        return branches
    }

    /**
     * Get a list of changed files.
     * @param options A map of options or named parameters
     * @param options.target The revision to compare to.
     * @param options.source The source revision, defaults to HEAD.
     * @param options.filter Optional values passed directly to git cli {@code --diff-filter} flag.
     * @return The result of the command.
     */
    List<String> changedFiles(Map<String,String> options) {
        if (!options.target) {
            this.log.error('You must specify a target revision to compare to.')
            throw new Exception('No target supplied.')
        }

        String branchSource = options.source ?: 'HEAD'
        String branchTarget = options.target
        String filter = options.filter ? "--diff-filter=${options.filter}" : ''

        Result result = this.executeGit("diff --name-only ${filter} ${branchSource} ${branchTarget} | paste -sd ',' -")

        List<String> files = result.stdOut.trim().split(',')

        return files
    }

    /**
     * Execute a git command.
     * @param args The options to pass to git.
     * @return The result of the command.
     */
    private Result executeGit(String args) {
        Result result

        // This passes because of Pipe Fail!
        try {
            result = this.bash.silent("git ${args}")
        } catch (ScriptError ex) {
            this.log.debug(ex)
            throw new Exception(ex.stdErr)
        }

        return result
    }

}
