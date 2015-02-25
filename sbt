#!/usr/bin/env bash
export JAVA_HOME=/data00/oph/java/jdk1.7.0_45
export JAVA8_HOME=/data00/oph/java/jdk1.8.0_05
./webbuild.sh
mvn=/opt/java/apache-maven-3.0.4/bin/mvn
$mvn clean package -Dmvn=$mvn
