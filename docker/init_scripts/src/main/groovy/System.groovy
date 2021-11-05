import jenkins.model.Jenkins
import jenkins.model.JenkinsLocationConfiguration
import hudson.tasks.Mailer

println('==== System Configuration')

// We do not wait for anything
Jenkins.instance.quietPeriod = 0

// get Jenkins location configuration
JenkinsLocationConfiguration config = JenkinsLocationConfiguration.get()

config.url = 'http://localhost:5050'
config.adminAddress = 'admin@non.existent.email'
Mailer.descriptor().defaultSuffix = '@non.existent.email'

config.save()
println('== System Configuration complete')
