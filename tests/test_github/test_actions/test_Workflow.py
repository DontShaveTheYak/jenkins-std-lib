import pytest

from tests.conftest import Container

@pytest.fixture()
def job_folder():
    return 'github/actions'

def test_workflow_example(container: Container, job_folder):

    job_output = container(f"{job_folder}/workflow_example.groovy")

    assert "[CI/test]" in job_output


def test_install_run(container: Container, job_folder):

    container(f"{job_folder}/tests/test_workflow.groovy")
