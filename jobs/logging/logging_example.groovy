/* groovylint-disable DuplicateMapLiteral, DuplicateStringLiteral, UnusedVariable */
@Library('jenkins-std-lib')

import org.dsty.logging.LogClient

node() {
    String cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    LogClient log = new LogClient()

    String level = 'default'

    log.debug(level)
    log.info(level)
    log.warn(level)
    log.error(level)

    level = 'DEBUG'
    env.PIPELINE_LOG_LEVEL = level

    log.debug(level)
    log.info(level)
    log.warn(level)
    log.error(level)

    level = 'INFO'
    env.PIPELINE_LOG_LEVEL = level

    log.debug(level)
    log.info(level)
    log.warn(level)
    log.error(level)

    level = 'WARN'
    env.PIPELINE_LOG_LEVEL = level

    log.debug(level)
    log.info(level)
    log.warn(level)
    log.error(level)

    level = 'ERROR'
    env.PIPELINE_LOG_LEVEL = level

    log.debug(level)
    log.info(level)
    log.warn(level)
    log.error(level)

    level = 'NONE'
    env.PIPELINE_LOG_LEVEL = level

    log.debug(level)
    log.info(level)
    log.warn(level)
    log.error(level)

    cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}
