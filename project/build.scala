import sbt._
import Keys._
import sbtbuildinfo.Plugin._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._
import com.earldouglas.xsbtwebplugin.WebPlugin
import com.earldouglas.xsbtwebplugin.WebPlugin.container
import com.earldouglas.xsbtwebplugin.PluginKeys._

object OmatsivutBuild extends Build {
  val Organization = "fi.vm.sade"
  val Name = "omatsivut"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.11.1"
  val ScalatraVersion = "2.3.0.RC3"

  // task for running mocha tests
  lazy val mocha = taskKey[Int]("run phantomJS tests")

  val mochaTask = mocha <<= (start in container.Configuration) map {
    Unit => {
      val pb = Seq("node_modules/mocha-phantomjs/bin/mocha-phantomjs" ,"-R", "spec", "http://localhost:8080/test/runner.html")
      val res = pb.!
      if(res != 0){
        sys.error("mocha tests failed")
      }
      res
    }
  }

  lazy val project = Project (
    "omatsivut",
    file("."),
    settings = Defaults.coreDefaultSettings ++ WebPlugin.webSettings ++ buildInfoSettings ++ mochaTask
      ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
      scalacOptions ++= Seq("-target:jvm-1.7", "-deprecation"),
      resolvers += Classpaths.typesafeReleases,
      sourceGenerators in Compile <+= buildInfo,
      buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
      EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed,
      buildInfoPackage := "fi.vm.sade.omatsivut",
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-json" % ScalatraVersion,
        "org.scalatra" %% "scalatra-swagger" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "junit" % "junit" % "4.11" % "test",
        "ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime",
        "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
        "org.scalaj" %% "scalaj-http" % "0.3.15",
        "org.apache.tomcat.embed" % "tomcat-embed-core"         % "7.0.22" % "container;test",
        "org.apache.tomcat.embed" % "tomcat-embed-logging-juli" % "7.0.22" % "container;test",
        "org.apache.tomcat.embed" % "tomcat-embed-jasper"       % "7.0.22" % "container;test",
        "org.mongodb" %% "casbah" % "2.7.2",
        "org.json4s" %% "json4s-jackson" % "3.2.10",
        "org.json4s" %% "json4s-ext" % "3.2.10",
        "com.typesafe" % "config" % "1.2.1",
        "com.novus" %% "salat-core" % "1.9.8",
        "commons-codec" % "commons-codec" % "1.9"
      ),
      artifactName <<= (name in (Compile, packageWar)) { projectName =>
        (config: ScalaVersion, module: ModuleID, artifact: Artifact) =>
          var newName = projectName
          if (module.revision.nonEmpty) {
            newName += "-" + module.revision
          }
          newName + "." + artifact.extension
      },
      artifactPath in (Compile, packageWar) ~= { defaultPath =>
        file("target") / defaultPath.getName
      },
      testOptions in Test += Tests.Argument("junitxml")
    )
  )
}
