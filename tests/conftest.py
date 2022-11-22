import os
from pathlib import Path
from typing import Callable, Generator, Optional
import shutil
from time import sleep

import docker
import pytest

Container = Callable[[str], str]


@pytest.fixture(scope="session")
def client():
    return docker.from_env()


@pytest.fixture(scope="session")
def container(client: docker.DockerClient) -> Generator[Container, None, None]:
    """Creates a jenkinsfile runner container and then Yields a function that
    can run jenkins jobs on the already started container.

    Args:
        client (docker.DockerClient): The docker client to manage containers.

    Yields:
        Generator[Callable[[str], str], None, None]: A function that will run Jenkins jobs.
    """

    jobs_path: str = str(Path(__file__, "../../jobs").resolve())
    lib_path: str = str(Path(__file__, "../../").resolve())
    # tmp_home: str = '/tmp/jenkins_home'

    (baseImage, _) = client.images.build(
        path=lib_path, dockerfile="docker/prod/Dockerfile", tag="jsl_prod", rm=True
    )

    (image, _) = client.images.build(
        path=lib_path,
        dockerfile="docker/jfr/Dockerfile",
        tag="jsl_jfr",
        buildargs={"baseImage": baseImage.id},
        rm=True,
    )

    volumes = {
        jobs_path: {"bind": "/workspace", "mode": "ro"},
        lib_path: {"bind": "/var/jenkins_home/pipeline-library", "mode": "rw"},
    }

    container = client.containers.run(
        image.id, tty=True, detach=True, volumes=volumes, privileged=True
    )

    # We should really do some kinda check to see if docker daemon is up and running =/
    sleep(30)

    def run_test(job_path: str) -> str:
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

        cmd = f"run_job {job_path}"

        exitcode, raw_output = container.exec_run(cmd, user="jenkins")

        output = raw_output.decode("utf-8")

        if exitcode:
            print(output)
            raise Exception(f"Container failed to run {job_path}.")

        return output

    yield run_test

    container.kill()
    container.remove()
