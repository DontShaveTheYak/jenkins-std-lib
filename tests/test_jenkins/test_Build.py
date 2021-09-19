from tests.conftest import Container

def test_build(container: Container):

    container('jenkins/build_example.groovy')
