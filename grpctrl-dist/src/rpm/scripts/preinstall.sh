#!/bin/sh

#
# The RPM pre-install script.
#

USERNAME="${project.user}"
GROUPNAME="${project.group}"

# Verify the existence of the group, creating it if it does not exist.
getent group ${GROUPNAME} >/dev/null || groupadd -r ${GROUPNAME}

# Verify the existence of the user, creating it if it does not exist.
getent passwd ${USERNAME} >/dev/null || \
    useradd -r -g ${GROUPNAME} -M -d /home/${USERNAME} -s /bin/bash \
    -c "User account for the ${project.groupId} system" ${USERNAME}

# Successful exit.
exit 0

