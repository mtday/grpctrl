#!/bin/sh

cd "$(dirname $0)"
rm -f *.jks *.crt

# Create a self signed key pair root CA certificate.
keytool -genkeypair -v \
  -alias localhost \
  -dname "CN=localhost, O=grpctrl-test, C=US" \
  -keystore keystore.jks \
  -keypass password \
  -storepass password \
  -keyalg RSA \
  -keysize 4096 \
  -ext KeyUsage="keyCertSign" \
  -ext BasicConstraints:"critical=ca:true" \
  -validity 9999


