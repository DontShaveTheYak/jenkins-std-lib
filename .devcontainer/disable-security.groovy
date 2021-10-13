#!groovy
/* groovylint-disable NoDef, NoWildcardImports, UnnecessaryGetter, UnnecessarySetter, VariableTypeRequired */
import jenkins.model.*
import hudson.security.*
import hudson.util.*
import jenkins.install.*
import javaposse.jobdsl.plugin.GlobalJobDslSecurityConfiguration
import jenkins.model.GlobalConfiguration

// You should not actually do this on your real jenkins.
println('==== Disable Jenkins Security')

println('== Disable JobDSL Security')
GlobalConfiguration.all().get(GlobalJobDslSecurityConfiguration).useScriptSecurity = false
GlobalConfiguration.all().get(GlobalJobDslSecurityConfiguration).save()

final Jenkins jenkins = Jenkins.getInstance()

println('== Disable Setup Wizard')
jenkins.setInstallState(InstallState.INITIAL_SETUP_COMPLETED)

println('== Disable Authentication')
def strategy = new AuthorizationStrategy.Unsecured()
jenkins.setAuthorizationStrategy(strategy)

jenkins.save()
println('== Disable Security complete')
