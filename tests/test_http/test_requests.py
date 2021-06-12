from tests.conftest import Container


def test_requests(container: Container):

    container('http/requests_example.groovy')
