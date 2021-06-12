from tests.conftest import Container

def test_plugins(container: Container):

    container('jenkins/instance_example.groovy')
