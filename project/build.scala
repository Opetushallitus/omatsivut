import com.earldouglas.xsbtwebplugin.PluginKeys._
import com.earldouglas.xsbtwebplugin.WebPlugin
import sbt.Keys._
import sbt._
import sbtbuildinfo.Plugin._

object OmatsivutBuild extends Build {
  val Organization = "fi.vm.sade"
  val Name = "omatsivut"
  val Version = "0.1.0-SNAPSHOT"
  val JavaVersion = "1.7"
  val ScalaVersion = "2.11.4"
  val ScalatraVersion = "2.3.0.RC3"
  val TomcatVersion = "7.0.22"
  val SpringVersion = "3.2.9.RELEASE"

  // task for running just unit tests. In scala these are not tagged.
  lazy val UnitTest = config("unit") extend Test

  // task for running just integration tests. In scala source code these are tagged: tag("integration")
  lazy val IntegrationTest = config("integration") extend Test

  if(!System.getProperty("java.version").startsWith(JavaVersion)) {
    throw new IllegalStateException("Wrong java version (required " + JavaVersion + "): " + System.getProperty("java.version"))
  }

  lazy val project = Project (
    "omatsivut",
    file("."),
    settings = Defaults.coreDefaultSettings ++ WebPlugin.webSettings ++ buildInfoSettings
      ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      javacOptions ++= Seq("-source", JavaVersion, "-target", JavaVersion),
      scalacOptions ++= Seq("-target:jvm-1.7", "-deprecation"),
      resolvers += Resolver.mavenLocal,
      resolvers += Classpaths.typesafeReleases,
      resolvers += "oph-sade-artifactory-releases" at "https://artifactory.oph.ware.fi/artifactory/oph-sade-release-local",
      resolvers += "oph-sade-artifactory-snapshots" at "https://artifactory.oph.ware.fi/artifactory/oph-sade-snapshot-local",
      sourceGenerators in Compile <+= buildInfo,
      parallelExecution in Test := false,
      buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
      buildInfoPackage := "fi.vm.sade.omatsivut",
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-json" % ScalatraVersion,
        "org.scalatra" %% "scalatra-swagger" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "junit" % "junit" % "4.11" % "test",
        "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
        "org.apache.tomcat.embed" % "tomcat-embed-core"         % TomcatVersion % "container;test",
        "org.apache.tomcat.embed" % "tomcat-embed-logging-juli" % TomcatVersion % "container;test",
        "org.apache.tomcat.embed" % "tomcat-embed-jasper"       % TomcatVersion % "container;test",
        "org.mongodb" %% "casbah" % "2.7.3",
        "com.novus" %% "salat-core" % "1.9.8",
        "com.scalatags" %% "scalatags" % "0.3.9",
        "commons-codec" % "commons-codec" % "1.9",
        "org.apache.commons" % "commons-compress" % "1.9",
        "fi.vm.sade" %% "scala-utils" % "0.1.0-SNAPSHOT",
        "fi.vm.sade" %% "scala-security" % "0.1.0-SNAPSHOT",
        "fi.vm.sade.koodisto" % "koodisto-api" % "13.3-SNAPSHOT",
        "fi.vm.sade.haku" % "hakemus-api" % "13.0-SNAPSHOT" excludeAll(
          ExclusionRule(organization = "org.json4s"),
          ExclusionRule(organization = "com.wordnik"),
          ExclusionRule(organization = "fi.vm.sade.koodisto")
        ),
        "fi.vm.sade.log" % "log-service" % "13.0-SNAPSHOT" classifier "classes" excludeAll(
          ExclusionRule(organization = "org.springframework.data"),
          ExclusionRule(organization = "org.springframework"),
          ExclusionRule(organization = "fi.vm.sade.generic")
        ),
        "fi.vm.sade" %% "scala-group-emailer" % "0.1.0-SNAPSHOT",
        "com.sun.jersey" % "jersey-client" % "1.17.1", // <- patch for transitive dependency problem
        "org.springframework" % "spring-jms" % SpringVersion, // <- patch for spring-core-3.1.3 transitive dep
        "org.springframework" % "spring-core" % SpringVersion,
        "org.springframework" % "spring-context" % SpringVersion,
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
        Tests.Argument("junitxml", "console")
      )
    )
  ).settings(
    net.virtualvoid.sbt.graph.Plugin.graphSettings: _*
  ).configs(UnitTest)
  .settings(inConfig(UnitTest)(Defaults.testTasks): _*)
  .settings(testOptions in UnitTest := Seq(
      Tests.Setup(() => {
        System.setProperty("specs2.outDir", "target/unit-specs2-reports")
        System.setProperty("specs2.junit.outDir", "target/unit-test-reports")
      }),
      Tests.Argument("junitxml", "console"),
      Tests.Argument("exclude", "integration")
    )
  ).configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.testTasks): _*)
  .settings(testOptions in IntegrationTest := Seq(
      Tests.Setup(() => {
        System.setProperty("specs2.outDir", "target/integration-specs2-reports")
        System.setProperty("specs2.junit.outDir", "target/integration-test-reports")
      }),
      Tests.Argument("junitxml", "console"),
      Tests.Argument("include", "integration")
    )
  )
  lazy val projectRef: ProjectReference = project
}
