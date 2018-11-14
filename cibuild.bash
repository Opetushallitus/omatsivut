#!/usr/bin/env bash

set -eo pipefail

time ./webbuild.sh
#EXTRA_MAVEN_ARGS="-DskipTests"
#EXTRA_MAVEN_ARGS="-Dtest=HakemusPreviewSpec"
time mvn clean install $EXTRA_MAVEN_ARGS -B -Dbranch=${TRAVIS_BRANCH} -Drevision=${TRAVIS_COMMIT} -DbuildNumber=${TRAVIS_BUILD_NUMBER} -Dvalintatulos.it.postgres.port=5432 -Domatsivut.it.postgres.alreadyrunning=true -Domatsivut.it.postgres.port=5432
echo "Checking what the local valinta-tulos-service war looks like"
ls -l ./target/valinta-tulos-service.war
#unzip -v ./target/valinta-tulos-service.war
mv -i target/omatsivut*SNAPSHOT.jar $DOCKER_BUILD_DIR/artifact/${ARTIFACT_NAME}.jar
cp -vair src/main/resources/oph-configuration $DOCKER_BUILD_DIR/config/
