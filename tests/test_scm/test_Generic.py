import pytest

from tests.conftest import Container


@pytest.fixture()
def job_folder():
    return "scm"


def test_generic_unit(container: Container, job_folder):

    container(f"{job_folder}/tests/test_generic.groovy")


@pytest.mark.xfail(
    strict=True,
    reason="Broken because of https://issues.jenkins.io/browse/JENKINS-70025",
)
def test_generic_example(container: Container, job_folder):

    job_output = container(f"{job_folder}/generic_example.groovy")

    assert "origin/master" in job_output
    assert "origin/develop" in job_output
