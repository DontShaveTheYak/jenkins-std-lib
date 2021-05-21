import pytest

@pytest.fixture()
def job_folder():
    return 'github/actions'

@pytest.mark.xfail(strict=True, reason='Bug in latest version of act.')
def test_workflow_example(container, job_folder):

    job_output = container(f"{job_folder}/workflow_example.groovy")

    assert b"[CI/test]" in job_output


def test_install_run(container, job_folder):

    job_output = container(f"{job_folder}/tests/test_workflow.groovy")
