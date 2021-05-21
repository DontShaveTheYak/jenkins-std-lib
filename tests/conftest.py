import os
from pathlib import Path
from typing import ByteString
import shutil

import docker
import pytest

@pytest.fixture(scope='session')
def client():
    return docker.from_env()

@pytest.fixture(scope='session')
def container(client: docker.DockerClient):

    image = 'shadycuz/jenkins-std-lib'

    jobs_path: str = str(Path(__file__, '../../jobs').resolve())
    lib_path: str = str(Path(__file__, '../../').resolve())
    tmp_home: str = '/tmp/jenkins_home'

    shutil.rmtree(tmp_home, ignore_errors=True)
    shutil.copytree(lib_path, f"{tmp_home}/pipeline-library", dirs_exist_ok=True,)

    volumes = {
        jobs_path: {
            'bind': '/workspace',
            'mode': 'ro'
        },
        tmp_home: {
            'bind': '/var/jenkins_home',
            'mode': 'rw'
        },
        '/var/run/docker.sock': {
            'bind': '/var/run/docker.sock',
            'mode': 'rw'
        }
    }

    container = client.containers.run(image,tty=True, entrypoint='bash',detach=True, volumes=volumes)

    def run_test(job_name):

        exitcode: int
        output: ByteString

        exitcode, output = container.exec_run(f"/app/bin/jenkinsfile-runner -w /app/jenkins -p /usr/share/jenkins/ref/plugins --runWorkspace /var/jenkins_home/workspace/job -f /workspace/{job_name}")

        if exitcode:
            print(output.decode('utf-8'))
            raise Exception(f"Container failed to run {job_name}.")

        return output

    yield run_test

    container.kill()
    container.remove()
