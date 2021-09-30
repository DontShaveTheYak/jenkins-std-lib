import pytest

from tests.conftest import Container

@pytest.fixture()
def job_folder():
    return 'system'

def test_platform_example(container: Container, job_folder):

    job_output = container(f"{job_folder}/platform_example.groovy")

    assert 'Ran on' in job_output
