import pytest

from tests.conftest import Container

@pytest.fixture()
def job_folder():
    return 'system/os/download/tests'

def test_archive_retriever(container: Container, job_folder):

    job_output = container(f"{job_folder}/test_ArchiveRetriever.groovy")

    assert "Extracting to" in job_output

def test_http_retriever(container: Container, job_folder):

    container(f"{job_folder}/test_HttpRetriever.groovy")
