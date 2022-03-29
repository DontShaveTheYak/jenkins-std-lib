import jenkins.model.Jenkins;
import org.jenkinsci.plugins.simpletheme.CssTextThemeElement;

Jenkins jenkins = Jenkins.get()

org.codefirst.SimpleThemeDecorator themeDecorator = jenkins.getExtensionList(org.codefirst.SimpleThemeDecorator.class).first()

final String cssOverides = '''\
.pipeline-annotated {
   display: none;
}
.pipeline-new-node {
      display: none;
}
'''

themeDecorator.setElements([
  new CssTextThemeElement(cssOverides)
])

jenkins.save()
