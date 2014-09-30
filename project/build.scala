import java.io.IOException
import java.net.Socket
import java.nio.file.{Paths, Files}
import com.earldouglas.xsbtwebplugin.PluginKeys.start
import scala.sys.process.Process
import sbt._
import Keys._
import sbtbuildinfo.Plugin._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._
import com.earldouglas.xsbtwebplugin.WebPlugin
import com.earldouglas.xsbtwebplugin.WebPlugin.container
import com.earldouglas.xsbtwebplugin.PluginKeys._

class MochaException extends RuntimeException("mocha tests failed", null, false, false)
object OmatsivutBuild extends Build {
  val Organization = "fi.vm.sade"
  val Name = "omatsivut"
  val Version = "0.1.0-SNAPSHOT"
  val JavaVersion = "1.7"
  val ScalaVersion = "2.11.1"
  val ScalatraVersion = "2.3.0.RC3"
  val TomcatVersion = "7.0.22"
  val SpringVersion = "3.2.9.RELEASE"

  // task for running mocha tests
  lazy val mocha = taskKey[Int]("run phantomJS tests")

  val mochaTask = mocha <<= (start in container.Configuration) map { _ =>
    val valintatulosService = ValintatulosServiceRunner.start
    val pb = Seq("node_modules/mocha-phantomjs/bin/mocha-phantomjs" ,"-R", "spec", "http://localhost:8080/omatsivut/test/runner.html")
    val res = pb.!
    valintatulosService.foreach(_.destroy)
    if(res != 0){
      throw new MochaException()
    }
    res
  }

  if(!System.getProperty("java.version").startsWith(JavaVersion)) {
    throw new IllegalStateException("Wrong java version (required " + JavaVersion + "): " + System.getProperty("java.version"))
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
      javacOptions ++= Seq("-source", JavaVersion, "-target", JavaVersion),
      scalacOptions ++= Seq("-target:jvm-1.7", "-deprecation"),
      resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
      resolvers += Classpaths.typesafeReleases,
      resolvers += "oph-sade-artifactory-releases" at "http://penaali.hard.ware.fi/artifactory/oph-sade-release-local",
      resolvers += "oph-sade-artifactory-snapshots" at "http://penaali.hard.ware.fi/artifactory/oph-sade-snapshot-local",
      sourceGenerators in Compile <+= buildInfo,
      parallelExecution in Test := false,
      buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
      EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed + EclipseCreateSrc.Resource,
      EclipseKeys.eclipseOutput := Some("target/eclipse"),
      EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE17),
      EclipseKeys.projectFlavor := EclipseProjectFlavor.Scala,
      buildInfoPackage := "fi.vm.sade.omatsivut",
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-json" % ScalatraVersion,
        "org.scalatra" %% "scalatra-swagger" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "junit" % "junit" % "4.11" % "test",
        "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
        "org.scalaj" %% "scalaj-http" % "0.3.15",
        "org.apache.tomcat.embed" % "tomcat-embed-core"         % TomcatVersion % "container;test",
        "org.apache.tomcat.embed" % "tomcat-embed-logging-juli" % TomcatVersion % "container;test",
        "org.apache.tomcat.embed" % "tomcat-embed-jasper"       % TomcatVersion % "container;test",
        "org.mongodb" %% "casbah" % "2.7.3",
        "org.json4s" %% "json4s-jackson" % "3.2.10",
        "org.json4s" %% "json4s-ext" % "3.2.10",
        "com.typesafe" % "config" % "1.2.1",
        "com.novus" %% "salat-core" % "1.9.8",
        "org.scalatra.scalate" %% "scalate-core" % "1.7.0",
        "com.scalatags" %% "scalatags" % "0.3.9",
        "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.4.1",
        "commons-codec" % "commons-codec" % "1.9",
        "fi.vm.sade.haku" % "hakemus-api" % "11.4-SNAPSHOT" excludeAll(
          ExclusionRule(organization = "org.json4s"),
          ExclusionRule(organization = "com.wordnik")
        ),
        "fi.vm.sade.log" % "log-service" % "8.0-SNAPSHOT" classifier "classes" excludeAll(
          ExclusionRule(organization = "org.springframework.data"),
          ExclusionRule(organization = "org.springframework"),
          ExclusionRule(organization = "fi.vm.sade.generic")
        ),
        "com.sun.jersey" % "jersey-client" % "1.17.1", // <- patch for transitive dependency problem
        "org.springframework" % "spring-jms" % SpringVersion, // <- patch for spring-core-3.1.3 transitive dep
        "org.springframework" % "spring-core" % SpringVersion,
        "org.springframework" % "spring-context" % SpringVersion,
        "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "1.46.0",
        "com.google.guava" % "guava" % "15.0"
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
      testOptions in Test := Seq(
        Tests.Argument("junitxml", "console"),
        Tests.Argument("exclude", "skipped")
      )
    ) ++ container.deploy(
      "/omatsivut" -> projectRef
    )
  ).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
  lazy val projectRef: ProjectReference = project
}

object ValintatulosServiceRunner {
  val valintatulosPort = 8097
  val searchPaths = List("./valinta-tulos-service", "../valinta-tulos-service")

  def start = {
    if (PortChecker.isFreeLocalPort(valintatulosPort)) {
      findValintatulosService match {
        case Some(path) => {
          val cwd = new java.io.File(path)
          val javaHome = System.getProperty("JAVA8_HOME", "")
          Process(List("./sbt", "test:compile"), cwd, "JAVA_HOME" -> javaHome).!
          val process = Process(List("./sbt", "test:run-main fi.vm.sade.valintatulosservice.JettyLauncher", "-Dvalintatulos.profile=it-externalHakemus"), cwd, "JAVA_HOME" -> javaHome).run(true)
          for (i <- 0 to 60 if PortChecker.isFreeLocalPort(valintatulosPort)) {
            Thread.sleep(1000)
          }
          Some(process)
        }
        case _ =>
          None
      }
    } else
      None
  }

  private def findValintatulosService = {
    searchPaths.find((path) => Files.exists(Paths.get(path)))
  }
}

object PortChecker {
  def isFreeLocalPort(port: Int): Boolean = {
    try {
      val socket = new Socket("127.0.0.1", port)
      socket.close()
      false
    } catch {
      case e:IOException => true
    }
  }
}
