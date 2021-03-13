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
    
