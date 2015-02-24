#!/usr/bin/env bash
export JAVA_HOME=/data00/oph/java/jdk1.7.0_45
export MAVEN_OPTS="-DJAVA8_HOME=$JAVA8_HOME"
./webbuild.sh
/opt/java/apache-maven-3.0.4/bin/mvn clean package
