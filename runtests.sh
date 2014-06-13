#!/bin/sh
./sbt mocha
result=$?
./sbt container:stop
exit ${result}
