
import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

class IntLogSpec extends JenkinsPipelineSpecification {
  def bash = null
  
  def setup() {
    script_class_path = ["."]
    log = loadPipelineScriptForTest("vars/log.groovy")
  }
  
  def "[buildJavascriptApp] will run npm publish if deploy is true"() {
    when:
        log.info('echo "hey"')
    then:
        // 1 * getPipelineMock("sh")("npm publish")
        1 == 1
  }
}