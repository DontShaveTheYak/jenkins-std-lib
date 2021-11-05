import pytest

from tests.conftest import Container

@pytest.fixture()
def job_folder():
    return 'system/os/programs'

def test_script_installer(container: Container, job_folder):

    job_output = container(f"{job_folder}/ScriptInstaller_example.groovy")

    install_msg = 'checking GitHub'

    # Test that the tool was installed twice
    assert job_output.count(install_msg) == 2

def test_url_installer(container: Container, job_folder):

    job_output = container(f"{job_folder}/UrlInstaller_example.groovy")

    install_msg = 'Extracting to'

    # Test that the tool was installed twice
    assert job_output.count(install_msg) == 2
