#!/bin/sh

cd "$(dirname $0)"
rm -f *.jks *.crt

# Create a self signed key pair root CA certificate.
keytool -genkeypair -v \
  -alias localhost \
  -dname "CN=localhost, O=grpctrl, C=US" \
  -keystore keystore.jks \
  -keypass password \
  -storepass password \
  -keyalg RSA \
  -keysize 4096 \
  -ext KeyUsage="keyCertSign" \
  -ext BasicConstraints:"critical=ca:true" \
  -validity 9999

# Export the public certificate so that it can be used in trust stores.
keytool -export -v \
  -alias localhost \
  -file localhost.crt \
  -keypass password \
  -storepass password \
  -keystore keystore.jks \
  -rfc

# Import the public certificate into a trust store.
keytool -importcert \
  -file localhost.crt \
  -keystore truststore.jks \
  -storepass password \
  -alias localhost \
  -noprompt

