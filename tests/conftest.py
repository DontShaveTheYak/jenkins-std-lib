import os
from pathlib import Path
from typing import ByteString

import docker
import pytest

@pytest.fixture(scope='module')
def client():
    return docker.from_env()

@pytest.fixture(scope='module')
def container(client: docker.DockerClient):

    image = os.getenv('RUNNER_IMAGE', 'iorunner')

    jobs_path: str = str(Path(__file__, '../../jobs').resolve())
    lib_path: str = str(Path(__file__, '../../').resolve())

    def create_container(job_name):

        volumes = {
            jobs_path: {
                'bind': '/workspace',
                'mode': 'ro'
            },
            lib_path: {
                'bind': '/var/jenkins_home/pipeline-library',
                'mode': 'ro'
            },
            '/var/run/docker.sock': {
                'bind': '/var/run/docker.sock',
                'mode': 'rw'
            }
        }

        return client.containers.run(image, f'-f /workspace/{job_name}', remove=True, volumes=volumes)


    return create_container
