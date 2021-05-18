import pytest

@pytest.fixture()
def job_folder():
    return 'github/actions'

def test_step_example(container, job_folder):

    job_output = container(f"{job_folder}/step_example.groovy")

    assert b"Hello Mona the Octocat" in job_output


def test_step_logic(container, job_folder):

    job_output = container(f"{job_folder}/tests/test_step.groovy")

    assert b"Skipping step." in job_output
