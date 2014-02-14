import sbt._
import Keys._
import org.sbtidea.SbtIdeaPlugin._
import xerial.sbt.Pack._

object ExodusBuild extends Build {
  import BuildSettings._
  import Dependencies._

  lazy val root = Project(id = "scala_learn",
    base = file("."))
    .settings(rootSettings: _*)
    .settings(libraryDependencies ++=
            rootDependencies)
}

object BuildSettings {

  val scalaV = "2.10.3"

  lazy val basicSettings = seq(
    organization          := "com.zavakid.scala.learn",
    startYear             := Some(2014),
    licenses              := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    scalaVersion          := scalaV,
    // sbt-idea: donot download javadoc, we donot like them
    ideaExcludeFolders := ".idea" :: ".idea_modules" :: Nil,
    transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
    testOptions in Test := Seq(Tests.Filter(s => s.endsWith("Test") || s.endsWith("Spec"))),
    scalacOptions := Seq(
      "-deprecation",
      "-unchecked",
      "-encoding", "UTF-8",
      "-target:jvm-1.7",
      "-Xlint",
      "-Yclosure-elim",
      "-Yinline",
      "-feature",
      "-language:postfixOps"
      // "-optimise"   // this option will slow our build
      ),
    javacOptions := Seq(
      "-target", "1.7" ,
      "-source", "1.7",
      "-Xlint:unchecked",
      "-Xlint:deprecation"
      ),
    resolvers ++= Seq(
      "99-empty" at "http://version99.qos.ch/"
      )
    ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ packSettings

  lazy val rootSettings = basicSettings ++ Seq(
    resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
    ,includeFilter in unmanagedJars := ("*.jar" - "*-sources.jar" ) | "*.so" | "*.dll" | "*.jnilib" | "*.zip"
  ) 

}

object Dependencies {

  val jettyVersion = "9.1.0.v20131115"
  val akkaVersion = "2.2.3"
  val sprayVersion = "1.2.0"


  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")


  def rootDependencies = logSuit ++ test(scalaTest, scalaMock, mockito) ++ Seq[ModuleID](
  ) ++ commons ++ jetty ++ akka ++ spray

  val httpClient = "org.apache.httpcomponents" % "fluent-hc" % "4.3.1"
  val scalaTest =  "org.scalatest" %% "scalatest" % "2.0"
  val scalaMock = "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1"
  val mockito = "org.mockito" % "mockito-all" % "1.9.5"
  val config = "com.typesafe" % "config" % "1.0.2"
  val protobuf = "com.google.protobuf" % "protobuf-java" % "2.4.1"
  val logSuit = Seq(
    "org.slf4j" % "log4j-over-slf4j" % "1.7.5"
    ,"org.slf4j" % "jcl-over-slf4j" % "1.7.5"
    ,"org.slf4j" % "slf4j-api" % "1.7.5"
    ,"ch.qos.logback" % "logback-classic" % "1.0.13"
    ,"commons-logging" % "commons-logging" % "99-empty"
    ,"log4j" % "log4j" % "99-empty"
    ,"com.typesafe" %% "scalalogging-slf4j" % "1.0.1"
  )

  val commons = Seq(
    "org.apache.commons" % "commons-lang3" % "3.2"
    ,"commons-io" % "commons-io" % "2.4"
    ,"commons-codec" % "commons-codec" % "1.9"
    ,"com.google.guava" % "guava" % "16.0"
    ,"com.google.code.findbugs" % "jsr305" % "2.0.3"
  )

  val jetty = Seq(
    "org.eclipse.jetty" % "jetty-webapp" % jettyVersion
  //,"javax.servlet.jsp" % "javax.servlet.jsp-api" % "2.3.1"
  )

  val akka = Seq(
    "com.typesafe.akka" %% "akka-remote" % akkaVersion
    ,"com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  )
  val spray = Seq(
    "io.spray" % "spray-routing" % sprayVersion
    ,"io.spray" % "spray-can" % sprayVersion
    )

}
