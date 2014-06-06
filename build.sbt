name := "omatsivut"

organization := "fi.vm.sade.omatsivut"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.1"

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

scalacOptions += "-target:jvm-1.7"

libraryDependencies ++= Seq(
  "javax.servlet"           % "javax.servlet-api"         % "3.0.1"  % "provided",
  "org.apache.tomcat.embed" % "tomcat-embed-core"         % "7.0.22" % "container",
  "org.apache.tomcat.embed" % "tomcat-embed-logging-juli" % "7.0.22" % "container",
  "org.apache.tomcat.embed" % "tomcat-embed-jasper"       % "7.0.22" % "container"
)

seq(webSettings :_*)