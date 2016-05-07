#!/bin/sh

#
# The RPM post-install script.
#


# Create a symlink for current to the newly installed version.
ln -sf /opt/${project.groupId}/${project.version} /opt/${project.groupId}/current

# Create a symlink for the /etc/init.d service script.
ln -sf /opt/${project.groupId}/current/bin/service.sh /etc/init.d/${project.groupId}

# Symlink the config directory.
ln -sf /etc/sysconfig/${project.groupId} /opt/${project.groupId}/current/config

# Symlink the home directory.
ln -sf /home/${project.user} /opt/${project.groupId}/current/home

# Symlink the logs directory.
ln -sf /var/log/${project.groupId} /opt/${project.groupId}/current/logs

# Create the keystore and truststore if they do not exist.
KEYSTORE="/home/${project.user}/pki/keystore.jks"
if [[ ! -f ${KEYSTORE} ]]; then
    # Create a self signed key pair root CA certificate.
    keytool -genkeypair -v \
      -alias localhost \
      -dname "CN=localhost, O=${project.groupId}, C=US" \
      -keystore ${KEYSTORE} \
      -storepass password \
      -keypass password \
      -keyalg RSA \
      -keysize 4096 \
      -ext KeyUsage="keyCertSign" \
      -ext BasicConstraints:"critical=ca:true" \
      -validity 9999

    chown ${project.user}:${project.group} ${KEYSTORE}
    chmod 640 ${KEYSTORE}
fi

