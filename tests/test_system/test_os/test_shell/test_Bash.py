from tests.conftest import Container

def test_bash_example(container: Container):

    job_output = container('system/os/shell/bash_example.groovy')

    # Test regular bash
    assert "Hello from Bash!" in job_output
    assert "RegularFakeCommand: command not found" in job_output

    # Test silent bash
    assert "NotShown!" not in job_output

    # Test ignoreErrors bash
    assert "fakecommand: command not found" in job_output
    assert "willNotRun" not in job_output
    assert "commandWillRun" in job_output
