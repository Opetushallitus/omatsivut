#!/usr/bin/env bash

set -eo pipefail

time ./webbuild.sh
#EXTRA_MAVEN_ARGS="-DskipTests"
#EXTRA_MAVEN_ARGS="-Dtest=HakemusPreviewSpec"
time mvn clean install -U $EXTRA_MAVEN_ARGS -B -Dbranch=${GITHUB_REF_NAME} -Drevision=${GITHUB_SHA} -DbuildNumber=${GITHUB_RUN_NUMBER}

