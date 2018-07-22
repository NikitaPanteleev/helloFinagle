lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    scroogeThriftSourceFolder in Compile := file("thrift")
  )


lazy val commonSettings = Seq(
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.12.6",
  organization := "Krowd9",
  organizationName := "krowd9",
  startYear := Some(2018),
  mainClass in Compile := Some("com.krowd9.userexp.Main"),
  cancelable in Global := true,
  licenses += ("Do What The Fuck You Want To Public License", url("http://www.wtfpl.net/")),
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-target:jvm-1.8",
    "-encoding", "UTF-8",
    "-Ypartial-unification",
    "-Ywarn-unused-import"
  ),
  testFrameworks += new TestFramework("utest.runner.Framework"),
  resolvers ++= Seq(
    //    "sonatype" at "https://oss.sonatype.org/content/groups/public"
  ),
  libraryDependencies ++= Seq(
    //finch is functional way to write Finagle service, so they say...
    "com.github.finagle" %% "finch-core" % "0.22.0",
    "com.github.finagle" %% "finch-circe" % "0.22.0",
    "io.circe" %% "circe-parser" % "0.9.0",
    "io.circe" %% "circe-generic" % "0.9.0",

    //twitter-scrooge is a Scala code generator for thrift files
    "org.apache.thrift" % "libthrift" % "0.11.0",
    "com.twitter" %% "scrooge-core" % "18.7.0" exclude("com.twitter", "libthrift"),
    "com.twitter" %% "finagle-thrift" % "18.7.0" exclude("com.twitter", "libthrift"),

    //test
    "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    "com.github.finagle" %% "finch-test" % "0.22.0" % Test,
    "org.mockito" % "mockito-core" % "2.7.22" % Test
  )
)