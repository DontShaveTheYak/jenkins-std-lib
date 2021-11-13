import os
from pathlib import Path
from typing import Callable, Generator, Optional
import shutil

import docker
import pytest

Container = Callable[[str], str]

@pytest.fixture(scope='session')
def client():
    return docker.from_env()

@pytest.fixture(scope='session')
def container(client: docker.DockerClient)-> Generator[Container, None, None]:
    """Creates a jenkinsfile runner container and then Yields a function that
    can run jenkins jobs on the already started container.

    Args:
        client (docker.DockerClient): The docker client to manage containers.

    Yields:
        Generator[Callable[[str], str], None, None]: A function that will run Jenkins jobs.
    """

    jobs_path: str = str(Path(__file__, '../../jobs').resolve())
    lib_path: str = str(Path(__file__, '../../').resolve())
    tmp_home: str = '/tmp/jenkins_home'

    (baseImage, _ ) = client.images.build(path=lib_path, dockerfile='docker/prod/Dockerfile', tag='jsl_prod', rm=True)

    (image, _ ) = client.images.build(path=lib_path, dockerfile='docker/jfr/Dockerfile', tag='jsl_jfr', buildargs={'baseImage': baseImage.id}, rm=True)

    shutil.rmtree(tmp_home, ignore_errors=True)
    shutil.copytree(lib_path, f"{tmp_home}/pipeline-library", dirs_exist_ok=True,)

    dind_path: Optional[str] = os.getenv('DIND_WORKSPACE')

    if dind_path:
        jobs_path = f"{dind_path}/jobs"
        lib_path = dind_path

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

    container = client.containers.run(image.id,tty=True, entrypoint='bash',detach=True, volumes=volumes)

    def run_test(job_path: str)-> str:
        """Runs the specified job using the jenkins runner.

        Args:
            job_path (str): The path to the job from inside the `jobs` directory.

        Raises:
            Exception: If the job fails.

        Returns:
            str: The job output.
        """
        exitcode: int
        raw_output: bytes

        cmd = (
            "/app/bin/jenkinsfile-runner -w /usr/share/jenkins "
            "--withInitHooks /usr/share/jenkins/ref/init.groovy.d "
            "-p /usr/share/jenkins/ref/plugins "
            "--runWorkspace /var/jenkins_home/workspace/job "
            f"-f /workspace/{job_path}"
        )

        exitcode, raw_output = container.exec_run(cmd, user='root')

        output = raw_output.decode('utf-8')

        if exitcode:
            print(output)
            raise Exception(f"Container failed to run {job_path}.")

        return output

    yield run_test

    container.kill()
    container.remove()
