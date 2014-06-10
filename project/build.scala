import sbt._
import Keys._
import org.scalatra.sbt._
import com.mojolly.scalate.ScalatePlugin._
import com.earldouglas.xsbtwebplugin.WebPlugin

object OmatsivutBuild extends Build {
  val Organization = "fi.vm.sade"
  val Name = "omatsivut"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.11.1"
  val ScalatraVersion = "2.3.0.RC3"

  lazy val project = Project (
    "omatsivut",
    file("."),
    settings = Defaults.defaultSettings ++ WebPlugin.webSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      unmanagedClasspath in Runtime += file(System.getProperty("user.home") + "/oph-configuration"),
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-json" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "junit" % "junit" % "4.11" % "test",
        "ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime",
        "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
        "org.scalaj" %% "scalaj-http" % "0.3.15",
        "org.apache.tomcat.embed" % "tomcat-embed-core"         % "7.0.22" % "container",
        "org.apache.tomcat.embed" % "tomcat-embed-logging-juli" % "7.0.22" % "container",
        "org.apache.tomcat.embed" % "tomcat-embed-jasper"       % "7.0.22" % "container",
        "org.mongodb" %% "casbah" % "2.7.2",
        "org.json4s" %% "json4s-jackson" % "3.2.9",
        "org.json4s" %% "json4s-ext" % "3.2.9",
        "com.typesafe" % "config" % "1.2.1",
        "com.novus" %% "salat-core" % "1.9.8"
      ),
      testOptions in Test += Tests.Argument("junitxml")
    )
  )
}
