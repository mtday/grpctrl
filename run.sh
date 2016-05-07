#!/usr/bin/env bash

cd $(dirname $0)

export SHARED_SECRET="abcdABCD1234!@#$"

mvn exec:java -pl :grpctrl-test -Dexec.mainClass="com.grpctrl.test.LocalRunner"

