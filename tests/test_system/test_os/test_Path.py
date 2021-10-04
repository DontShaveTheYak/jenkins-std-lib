import pytest

from tests.conftest import Container

@pytest.fixture()
def job_folder():
    return 'system/os/path'

def test_path_example(container: Container, job_folder):

    container(f"{job_folder}/path_example.groovy")
