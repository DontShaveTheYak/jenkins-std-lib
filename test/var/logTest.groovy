import org.junit.*
import static org.junit.Assert.*
import com.lesfurets.jenkins.unit.*
 
// Extend the BasePipelineTest to use the Jenkins Pipeline Unit framework
class logTest extends BasePipelineTest {
    def log
    String debug_prefix = '[Debug]'
    String info_prefix = '[Info]'
    String warn_prefix = '[Warning]'
    String error_prefix = '[Error]'
 
    // Before every testcase is run, do this:
    @Before
    void setUp() {
        super.setUp()
        helper.registerAllowedMethod("ansiColor", [String.class, Closure.class], null)
        helper.registerAllowedMethod("println", [String.class], null)
        log = loadScript("vars/log.groovy")
    }

    @Test
    void 'levelCheck should correctly determine when to log.'() {

        Boolean first = log.levelCheck([])

        binding.setVariable('env', [PIPELINE_LOG_LEVEL:"ERROR"])
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

        binding.setVariable('env', [PIPELINE_LOG_LEVEL:"ERROR"])
        log.debug(message)

        printCallStack()

        assertEquals('Failed to log the correct ammount of times', 2, helper.methodCallCount('println'))
        assertTrue('Failed to log correct format', helper.getCallStack()[2].args[0].toString().contains("${warn_prefix} ${message}"))
    }

    @Test
    void 'Info should log correctly'() {

        String message = 'This is only a Test!'
        log.info(message)

        assertEquals('Failed to log the correct ammount of times', 1, helper.methodCallCount('println'))
        assertTrue('Failed to log correct format', helper.getCallStack()[2].args[0].toString().contains("${info_prefix} ${message}"))
        helper.callStack.clear()

        binding.setVariable('env', [PIPELINE_LOG_LEVEL:"ERROR"])
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
        assertTrue('Failed to log correct format', helper.getCallStack()[2].args[0].toString().contains("${warn_prefix} ${message}"))
        helper.callStack.clear()

        binding.setVariable('env', [PIPELINE_LOG_LEVEL:"ERROR"])
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
        assertTrue('Failed to log correct format', helper.getCallStack()[2].args[0].toString().contains("${warn_prefix} ${message}"))
        helper.callStack.clear()

        binding.setVariable('env', [PIPELINE_LOG_LEVEL:"ERROR"])
        log.info(message)

        assertTrue('Should not have logged', helper.callStack.findAll { call ->
            call.methodName == 'println'
        }.isEmpty())
    }
}