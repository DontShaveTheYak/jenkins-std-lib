import pytest

from tests.conftest import Container


@pytest.fixture()
def job_folder():
    return "js/node"


def test_nodenv_example(container: Container, job_folder):

    _ = container(f"{job_folder}/nodenv_example.groovy")


def test_node_example(container: Container, job_folder):

    _ = container(f"{job_folder}/node_example.groovy")


def test_npm_example(container: Container, job_folder):

    _ = container(f"{job_folder}/npm_example.groovy")
