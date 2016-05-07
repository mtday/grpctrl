#!/bin/sh

yum install -y java-1.8.0-openjdk-devel
yum install -y vim
yum update -y nss


echo "Installing PostgreSQL"
yum localinstall -y https://download.postgresql.org/pub/repos/yum/9.5/redhat/rhel-6-x86_64/pgdg-centos95-9.5-2.noarch.rpm
yum install -y postgresql95
yum install -y postgresql95-server
service postgresql-9.5 initdb
chkconfig postgresql-9.5 on

cat <<EOF > /tmp/pg_hba.conf
local   all             all                                     peer
host    grpctrl         grpctrl         127.0.0.1/32            md5
EOF
mv /tmp/pg_hba.conf /var/lib/pgsql/9.5/data/pg_hba.conf

service postgresql-9.5 start
echo "source /etc/bashrc" >> /var/lib/pgsql/.bash_profile

cat <<EOF | su - postgres -c "psql -U postgres"
    CREATE DATABASE grpctrl;
    CREATE USER grpctrl WITH PASSWORD 'password';
    ALTER ROLE grpctrl WITH CREATEDB;
EOF

cat <<EOF > /tmp/psql.sh
alias psql="psql -h 127.0.0.1 -U grpctrl -W grpctrl"
EOF
mv /tmp/psql.sh /etc/profile.d/psql.sh


cat <<EOF > /tmp/java.sh
export JAVA_HOME="/usr/lib/jvm/java-1.8.0-openjdk.x86_64"
export PATH="\${JAVA_HOME}/bin:\${PATH}"
EOF
mv /tmp/java.sh /etc/profile.d/java.sh


echo "Installing Maven"
if [[ ! -d /opt/apache-maven ]]; then
    mkdir /opt/apache-maven
    wget -q -O /opt/apache-maven/apache-maven-3.3.9-bin.tar.gz \
            http://mirrors.gigenet.com/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
    tar xzf /opt/apache-maven/apache-maven-3.3.9-bin.tar.gz -C /opt/apache-maven
    ln -s /opt/apache-maven/apache-maven-3.3.9 /opt/apache-maven/current
fi
cat <<EOF > /tmp/maven.sh
export M2_HOME="/opt/apache-maven/current"
export PATH="\${M2_HOME}/bin:\${PATH}"
EOF
mv /tmp/maven.sh /etc/profile.d/maven.sh
chmod 444 /etc/profile.d/maven.sh


echo "Updating system config"
cat <<EOF > /tmp/sudoers
%grpctrl ALL=(ALL) NOPASSWD: ALL
EOF
mv /tmp/sudoers /etc/sudoers.d/grpctrl
chown root:root /etc/sudoers.d/grpctrl

cat <<EOF >> /etc/security/limits.conf
*                hard    nofile          65535
*                soft    nofile          65535
EOF


