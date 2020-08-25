/* groovylint-disable ClassJavadoc, ClosureAsLastMethodParameter, DuplicateNumberLiteral, DuplicateStringLiteral, MethodName, Println */

import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Before
import com.lesfurets.jenkins.unit.BasePipelineTest

class Testbash extends BasePipelineTest {

    Object bash

    @Before
    void setUp() {
        super.setUp()
        binding.setVariable('log', loadScript('vars/log.groovy'))
        helper.registerAllowedMethod('ansiColor', [String, Closure], null)
        helper.registerAllowedMethod('println', [String], null)
        bash = loadScript('vars/bash.groovy')
    }

    @Test
    void 'Should format scripts correctly.'() {
        String userScript = 'echo "hey"'
        String script = bash.formatScript(userScript)

        assertTrue('Should have set bash shabang.', script.contains('#!/bin/bash'))
        assertTrue('Should have sourced bashrc.', script.contains('source $HOME/.bashrc > /dev/null 2>&1 || true'))
        assertTrue('Should have set exit on first error.', script.contains('{ set -e; } > /dev/null 2>&1'))
        assertTrue('Should have set output to console.', script.contains('exec 2> >(tee -a stderr stdall) 1> >(tee -a stdout stdall)'))
        assertTrue('Should have not output the bash commands.', script.contains('{ set +x; } > /dev/null 2>&1'))
        assertTrue('Should have included our script.', script.contains(userScript))
        assertEquals('Should indent returned script.', script.stripIndent(), script)

        binding.setVariable('env', [PIPELINE_LOG_LEVEL:'DEBUG'])
        script = bash.formatScript(userScript, false, false)
        assertTrue('Should not exit on first error.', script.contains('{ set +e; } > /dev/null 2>&1'))
        assertTrue('Should not output to console.', script.contains('exec &> /dev/null'))
        assertTrue('Should output the bash commands.', script.contains('{ set -x; } > /dev/null 2>&1'))
        assertEquals('Should have debug logging.', 2, helper.methodCallCount('debug'))
    }

    @Test
    void 'Should implement call.'() {
      String userScript = 'echo "hey"'
      helper.registerAllowedMethod('sh', [Map], { c -> 0 })
      helper.registerAllowedMethod('readFile', [String], { 'hey' })

      Object result = bash(userScript)

      assertEquals('Should return a result object', 'Result', result.getClass().getSimpleName())
      assertTrue('Result should have our output.', result.toString() == 'hey')
    }

    @Test
    void 'Call should throw exception on error.'() {
      Object result
      String userScript = 'fakeCommand'
      helper.registerAllowedMethod('sh', [Map], { c -> 4 })
      helper.registerAllowedMethod('readFile', [String], { "/var/jenkins_home/workspace/Shared Library Dev@tmp/durable-6609e43d/script.sh: line 8: ${userScript }: command not found" })

      try {
        result = bash(userScript)
      /* groovylint-disable-next-line CatchException */
      } catch (Exception ex) {
        println('Caught Exception')
        result = ex
      }

      assertEquals('Should return a result object', 'ScriptError', result.getClass().getSimpleName())
      assertTrue('Result should have our output.', result.toString().contains("${userScript}: command not found"))
    }

    @Test
    void 'Should ignore errors.'() {
      String userScript = 'fakeCommand'
      helper.registerAllowedMethod('sh', [Map], { c -> 4 })
      helper.registerAllowedMethod('readFile', [String], { "/var/jenkins_home/workspace/Shared Library Dev@tmp/durable-6609e43d/script.sh: line 8: ${userScript }: command not found" })

      Object result = bash.ignoreErrors(userScript)

      assertEquals('Should return a result object', 'Result', result.getClass().getSimpleName())
      assertTrue('Result should have our output.', result.toString().contains("${userScript}: command not found"))
    }

    @Test
    void 'Should get script output.'() {
      helper.registerAllowedMethod('readFile', [String], { file ->
        if (file == 'stdout') {
          return 'stdOut'
        } else if (file == 'stderr') {
          return 'stdErr'
        }
        return 'output'
      })

      def(String stdOut, String stdErr, String output) = bash.readOutputs()

      assertTrue('Should read the stdout file.', helper.getCallStack()[1].args[0].toString().contains('stdout'))
      assertTrue('Should read the stderr file.', helper.getCallStack()[2].args[0].toString().contains('stderr'))
      assertTrue('Should read the stdall file.', helper.getCallStack()[3].args[0].toString().contains('stdall'))

      assertEquals('Should return stdOut', 'stdOut', stdOut)
      assertEquals('Should return stdErr', 'stdErr', stdErr)
      assertEquals('Should return output', 'output', output)
      assertTrue('Should cleanup log files.', helper.getCallStack()[4].args[0].toString().contains('rm stdout stderr stdall'))
    }

}
