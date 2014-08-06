#!/bin/bash -e
find node_modules -name ".git" | rev | cut -c6- | rev | xargs rm -fr
npm install
node_modules/gulp/bin/gulp.js compile
./sbt -batch mocha -java-home $JAVA_HOME -Domatsivut.profile=it
result=$?
./sbt -batch container:stop -java-home $JAVA_HOME
exit ${result}
