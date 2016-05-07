#!/bin/sh

#
# The RPM pre-remove script.
#


# Stop any running services.
service ${project.groupId} stop

# Delete the symlink for the config directory.
rm -f /opt/${project.groupId}/current/config

# Delete the symlink for the home directory.
rm -f /opt/${project.groupId}/current/home

# Delete the symlink for the logs directory.
rm -f /opt/${project.groupId}/current/logs


