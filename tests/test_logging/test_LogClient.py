def test_levels(container):

    job_output = container('logging/levels')

    assert b"[Debug] default" not in job_output
    assert b"[Info] default" in job_output
    assert b"[Warning] default" in job_output
    assert b"[Error] default" in job_output

    assert b"[Debug] DEBUG" in job_output
    assert b"[Info] DEBUG" in job_output
    assert b"[Warning] DEBUG" in job_output
    assert b"[Error] DEBUG" in job_output

    assert b"[Debug] INFO" not in job_output
    assert b"[Info] INFO" in job_output
    assert b"[Warning] INFO" in job_output
    assert b"[Error] INFO" in job_output

    assert b"[Debug] WARN" not in job_output
    assert b"[Info] WARN" not in job_output
    assert b"[Warning] WARN" in job_output
    assert b"[Error] WARN" in job_output

    assert b"[Debug] ERROR" not in job_output
    assert b"[Info] ERROR" not in job_output
    assert b"[Warning] ERROR" not in job_output
    assert b"[Error] ERROR" in job_output

    assert b"[Debug] NONE" not in job_output
    assert b"[Info] NONE" not in job_output
    assert b"[Warning] NONE" not in job_output
    assert b"[Error] NONE" not in job_output
