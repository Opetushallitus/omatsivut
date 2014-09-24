#!/bin/bash -e
if [[ -z "$JAVA8_HOME" ]]; then
  echo "Set JAVA8_HOME variable for valinta-tulos-service"
  exit 1
fi

find node_modules -name ".git" | rev | cut -c6- | rev | xargs rm -fr
npm install
node_modules/gulp/bin/gulp.js compile

./sbt -batch mocha -java-home $JAVA_HOME -Domatsivut.profile=it-with-valintatulos -Domatsivut.valinta-tulos-service.url=http://localhost:8097/valinta-tulos-service -Dvalintatulos.JAVA_HOME=$JAVA8_HOME
result=$?
./sbt -batch container:stop -java-home $JAVA_HOME
exit ${result}
