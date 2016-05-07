#!/bin/sh
#
# ${project.groupId}	Start the ${project.groupId} service
#
# chkconfig: 2345 40 40
# description:	Perform management operations on the ${project.groupId} service
#
# config: /etc/sysconfig/${project.groupId}
#
### BEGIN INIT INFO
# Provides: ${project.groupId}
# Required-Start:
# Required-Stop:
# Default-Start: 2 3 4 5
# Default-Stop: 0 1 6
# Short-Description: The ${project.groupId} service
# Description: Perform management operations on ${project.groupId} service
### END INIT INFO

# Source function library.
if [[ -f /etc/init.d/functions ]]; then
    . /etc/init.d/functions
fi

SERVICE="${project.groupId}"
SERVICE_USER="${project.user}"

CONFIG_DIR="/etc/sysconfig/${SERVICE}"
VAR_DIR="/var/run/${SERVICE}"


# Only usable for root.
if [ ${EUID} -ne 0 ]; then
    echo -n $"${SERVICE}: Only usable by root."; failure; echo
    exit 4
fi

# Check to see if the service is running.
# Sets the $PID variable to the running pid if running, and to 0 if not running.
_check_running() {
    PID_FILE="${VAR_DIR}/${SERVICE}.pid"

    # First check the pid file
    if [[ -f "${PID_FILE}" ]]; then
        # The file exists so presumably the service is running.
        PID="$(cat ${PID_FILE})"
        if [[ -z ${PID} ]]; then
            # The pid file exists but was empty for some reason.
            # Delete the pid file and return 0 to indicate not running.
            rm -f ${PID_FILE}
            PID=0 # not running
        else
            # Check to verify that the service pid is running.
            ps -p ${PID} >/dev/null
            IS_RUNNING=$? # 0 if running, 1 if not
            if [[ ${IS_RUNNING} -ne 0 ]]; then
                # The pid file exists, but the pid does not seem to be running.
                # Delete the pid file and return 0 to indicate not running.
                rm -f ${PID_FILE}
                PID=0 # not running
            fi
        fi
    else
        # The file does not exist so presumably the service is not running.
        PID=0 # not running
    fi
}

# Start the service.
_start() {
    # Check to see if the service is already running. Sets PID variable.
    _check_running

    if [[ ${PID} -ne 0 ]]; then
        echo -n "${SERVICE} already running with pid ${PID}"; success; echo
        return 0
    else
        RUN_SCRIPT="/opt/${SERVICE}/current/bin/run-service.sh"
        runuser -s /bin/bash ${SERVICE_USER} -c "${RUN_SCRIPT}"
        STARTED=$?

        if [[ ${STARTED} -eq 0 ]]; then
            # Verify that the service is running. Sets PID variable.
            _check_running

            if [[ ${PID} -ne 0 ]]; then
                echo -n "${SERVICE} started with pid ${PID}"; success; echo
                return 0
            else
                echo -n "${SERVICE} failed to start"; failure; echo
                return 1
            fi
        else
            echo -n "${SERVICE} failed to start"; failure; echo
            return 1
        fi
    fi
}

# Stop the service.
_stop() {
    # Check to see if the service is running. Sets PID variable.
    _check_running

    if [[ ${PID} -eq 0 ]]; then
        echo -n "${SERVICE} is not running"; success; echo
        return 0
    else
        # Kill gracefully with SIGTERM, which will usually be sufficient.
        kill -1 ${PID}

        # No longer running so remove the pid file.
        PID_FILE="${VAR_DIR}/${SERVICE}.pid"
        rm -f ${PID_FILE}

        echo -n "${SERVICE} stopped pid ${PID}"; success; echo
        return 0
    fi
}

# Check the status of the specified service.
_status() {
    # Check to see if the service is running. Sets PID variable.
    _check_running

    if [[ ${PID} -ne 0 ]]; then
        echo -n "${SERVICE} running on pid ${PID}"; success; echo
        return 0
    else
        echo -n "${SERVICE} is not running"; warning; echo
        return 1
    fi
}

# Restart the service.
_restart() {
    _stop
    _start
}

case "$1" in
    start)
        _start
        RETVAL=$?
        ;;

    stop)
        _stop
        RETVAL=$?
        ;;

    restart)
        _restart
        RETVAL=$?
        ;;

    status)
        _status
        RETVAL=$?
        ;;

    *)
        echo $"Usage: ${SERVICE} {start|stop|restart|status}"
        RETVAL=2
        ;;
esac

exit ${RETVAL}

