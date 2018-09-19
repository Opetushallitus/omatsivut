#!/usr/bin/env bash

set -eo pipefail

time ./webbuild.sh
#EXTRA_MAVEN_ARGS="-DskipTests"
time mvn clean install $EXTRA_MAVEN_ARGS -B -Dbranch=${TRAVIS_BRANCH} -Drevision=${TRAVIS_COMMIT} -DbuildNumber=${TRAVIS_BUILD_NUMBER} -Dvalintatulos.it.postgres.port=5432
mv -i target/omatsivut*SNAPSHOT.war $DOCKER_BUILD_DIR/artifact/${ARTIFACT_NAME}.war
cp -vair src/main/resources/oph-configuration $DOCKER_BUILD_DIR/config/
