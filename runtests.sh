#!/bin/sh
npm install
node_modules/gulp/bin/gulp.js compile
./sbt mocha -java-home $JAVA_HOME
result=$?
./sbt container:stop -java-home $JAVA_HOME
exit ${result}
