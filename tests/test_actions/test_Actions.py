def test_hello_world_docker(container):

    job_output = container('actions/hello_world_docker.groovy')

    assert b"Hello Jenkins-Std-Lib" in job_output
