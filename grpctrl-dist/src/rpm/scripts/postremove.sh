#!/bin/sh

#
# The RPM post-remove script.
#


# Delete the symlink for the current version.
rm -f /opt/${project.groupId}/current

# Delete the symlink for the /etc/init.d service script.
rm -f /etc/init.d/${project.groupId}


