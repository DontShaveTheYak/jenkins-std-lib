def test_bash_example(container):

    job_output = container('bash/bash_example.groovy')

    # Test regular bash
    assert b"Hello from Bash!" in job_output
    assert b"RegularFakeCommand: command not found" in job_output

    # Test silent bash
    assert b"NotShown!" not in job_output

    # Test ignoreErrors bash
    assert b"fakecommand: command not found" in job_output
    assert b"secretcommand" not in job_output
    assert b"anothercommand" not in job_output
