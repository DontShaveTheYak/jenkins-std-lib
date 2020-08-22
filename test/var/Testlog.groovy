/* groovylint-disable ClassJavadoc, DuplicateMapLiteral, DuplicateNumberLiteral, DuplicateStringLiteral, MethodName */
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertSame
import org.junit.Test
import org.junit.Before
import org.junit.Ignore
import com.lesfurets.jenkins.unit.BasePipelineTest

// Extend the BasePipelineTest to use the Jenkins Pipeline Unit framework
class Testlog extends BasePipelineTest {

    Object log
    String debugPrefix = '[Debug]'
    String infoPrefix = '[Info]'
    String warnPrefix = '[Warning]'
    String errorPrefix = '[Error]'

    // Before every testcase is run, do this:
    @Before
    void setUp() {
        super.setUp()
        helper.registerAllowedMethod('ansiColor', [String, Closure], null)
        helper.registerAllowedMethod('println', [String], null)
        log = loadScript('vars/log.groovy')
    }

    @Test
    void 'Should return a string for any object.'() {
        List list = ['list1', 'list2']
        Map map = [map1: 'value1', map2: 'value2']
        Integer number = 5
        String message = 'This is only a Test!'

        for (String item in [list, map, number, message]) {
            String result = log.getString(item)
            assertSame(result.getClass(), String)
        }
    }

    @Test
    void 'levelCheck should correctly determine when to log.'() {
        Boolean first = log.levelCheck([])

        binding.setVariable('env', [PIPELINE_LOG_LEVEL:'ERROR'])
        Boolean second = log.levelCheck(['ERROR'])

        assertFalse('Failed to prevent logging.', first)
        assertTrue('Failed to enable logging.', second)
    }

    @Test
    void 'Log only info/warn when no level is set.'() {
        log.debug('debug')
        log.info('testing')
        log.warn('warn')
        log.error('error')

        assertEquals('Failed to log the correct ammount of times', 2, helper.methodCallCount('println'))
    }

    @Ignore
    @Test
    void 'Debug should log correctly'() {
        String message = 'This is only a Test!'
        log.debug(message)

        binding.setVariable('env', [PIPELINE_LOG_LEVEL:'ERROR'])
        log.debug(message)

        printCallStack()

        assertEquals('Failed to log the correct ammount of times', 2, helper.methodCallCount('println'))
        assertTrue('Failed to log correct format', helper.getCallStack()[2].args[0].toString().contains("${warnPrefix} ${message}"))
    }

    @Test
    void 'Info should log correctly'() {
        String message = 'This is only a Test!'
        log.info(message)

        assertEquals('Failed to log the correct ammount of times', 1, helper.methodCallCount('println'))
        assertTrue('Failed to log correct format', helper.getCallStack()[2].args[0].toString().contains("${infoPrefix} ${message}"))
        helper.callStack.clear()

        binding.setVariable('env', [PIPELINE_LOG_LEVEL:'ERROR'])
        log.info(message)

        assertTrue('Should not have logged', helper.callStack.findAll { call ->
            call.methodName == 'println'
        }.isEmpty())
    }

    @Test
    void 'Warn should log correctly'() {
        String message = 'This is only a Test!'
        log.warn(message)

        assertEquals('Failed to log the correct ammount of times', 1, helper.methodCallCount('println'))
        assertTrue('Failed to log correct format', helper.getCallStack()[2].args[0].toString().contains("${warnPrefix} ${message}"))
        helper.callStack.clear()

        binding.setVariable('env', [PIPELINE_LOG_LEVEL:'ERROR'])
        log.info(message)

        assertTrue('Should not have logged', helper.callStack.findAll { call ->
            call.methodName == 'println'
        }.isEmpty())
    }

    @Ignore
    @Test
    void 'Error should log correctly'() {
        String message = 'This is only a Test!'
        log.error(message)

        assertEquals('Failed to log the correct ammount of times', 1, helper.methodCallCount('println'))
        assertTrue('Failed to log correct format', helper.getCallStack()[2].args[0].toString().contains("${warnPrefix} ${message}"))
        helper.callStack.clear()

        binding.setVariable('env', [PIPELINE_LOG_LEVEL:'ERROR'])
        log.info(message)

        assertTrue('Should not have logged', helper.callStack.findAll { call ->
            call.methodName == 'println'
        }.isEmpty())
    }

}
