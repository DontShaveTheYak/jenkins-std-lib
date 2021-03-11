import os
from pathlib import Path
from typing import ByteString

import docker
import pytest

@pytest.fixture(scope='session')
def client():
    return docker.from_env()

@pytest.fixture(scope='session')
def container(client: docker.DockerClient):

    image = os.getenv('RUNNER_IMAGE', 'iorunner')

    jobs_path: str = str(Path(__file__, '../../../jobs').resolve())
    lib_path: str = str(Path(__file__, '../../../').resolve())
    
    def create_container(job_name):

        volumes = {
            jobs_path: {
                'bind': '/workspace',
                'mode': 'ro'
            },
            lib_path: {
                'bind': '/var/jenkins_home/pipeline-library',
                'mode': 'ro'
            }
        }

        return client.containers.run(image, f'-f /workspace/{job_name}', remove=True, volumes=volumes)


    return create_container

def test_call(container):

    job_output = container('bash/call')

    assert b"TestMessage" in job_output
    assert b"fakecommand: command not found" in job_output

def test_silent(container):

    job_output = container('bash/silent')

    assert b"TestMessage" not in job_output
    assert b"fakecommand: command not found" not in job_output


def test_ignore_errors(container):

    job_output = container('bash/ignore_errors')

    assert b"fakecommand: command not found" in job_output
    assert b"secretcommand" not in job_output
    assert b"anothercommand" not in job_output
    
