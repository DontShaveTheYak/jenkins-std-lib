import pytest

from tests.conftest import Container


@pytest.fixture()
def job_folder():
    return "vscode"


def test_devcontainer_example(container: Container, job_folder):

    _ = container(f"{job_folder}/devcontainer_example.groovy")
