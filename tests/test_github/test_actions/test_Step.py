import pytest

from tests.conftest import Container

@pytest.fixture()
def job_folder():
    return 'github/actions'

def test_step_example(container: Container, job_folder):

    job_output = container(f"{job_folder}/step_example.groovy")

    assert "Hello DockerAction" in job_output
    assert "Hello JavaScriptAction" in job_output
    assert "Setting an output!" in job_output


def test_step_logic(container: Container, job_folder):

    job_output = container(f"{job_folder}/tests/test_step.groovy")

    assert "Skipping step." in job_output
