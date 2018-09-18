#!/usr/bin/env bash

set -eo pipefail

time ./webbuild.sh
time mvn clean install -Dbranch=${TRAVIS_BRANCH} -Drevision=${TRAVIS_COMMIT} -DbuildNumber=${TRAVIS_BUILD_NUMBER} -Dvalintatulos.it.postgres.port=15432
