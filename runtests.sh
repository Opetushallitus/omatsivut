#!/bin/sh
./sbt mocha -java-home $JAVA_HOME
result=$?
./sbt container:stop -java-home $JAVA_HOME
exit ${result}
