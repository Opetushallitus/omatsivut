#!/bin/bash -e
rm -fr node_modules
npm install
node_modules/gulp/bin/gulp.js compile
./sbt mocha -java-home $JAVA_HOME -Domatsivut.profile=it
result=$?
./sbt container:stop -java-home $JAVA_HOME
exit ${result}
