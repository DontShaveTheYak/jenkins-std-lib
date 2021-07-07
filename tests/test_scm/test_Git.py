import pytest

from tests.conftest import Container

@pytest.fixture()
def job_folder():
    return 'scm'

def test_git_example(container: Container, job_folder):

    container(f"{job_folder}/git_example.groovy")
