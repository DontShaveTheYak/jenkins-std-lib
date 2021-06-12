from tests.conftest import Container

def test_levels(container: Container):

    job_output = container('logging/logging_example.groovy')

    assert "[Debug] default" not in job_output
    assert "[Info] default" in job_output
    assert "[Warning] default" in job_output
    assert "[Error] default" in job_output

    assert "[Debug] DEBUG" in job_output
    assert "[Info] DEBUG" in job_output
    assert "[Warning] DEBUG" in job_output
    assert "[Error] DEBUG" in job_output

    assert "[Debug] INFO" not in job_output
    assert "[Info] INFO" in job_output
    assert "[Warning] INFO" in job_output
    assert "[Error] INFO" in job_output

    assert "[Debug] WARN" not in job_output
    assert "[Info] WARN" not in job_output
    assert "[Warning] WARN" in job_output
    assert "[Error] WARN" in job_output

    assert "[Debug] ERROR" not in job_output
    assert "[Info] ERROR" not in job_output
    assert "[Warning] ERROR" not in job_output
    assert "[Error] ERROR" in job_output

    assert "[Debug] NONE" not in job_output
    assert "[Info] NONE" not in job_output
    assert "[Warning] NONE" not in job_output
    assert "[Error] NONE" not in job_output
