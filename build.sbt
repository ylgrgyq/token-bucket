
scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-optimise",
  "-Yinline-warnings"
)

fork := true

javaOptions += "-Xmx2G"

parallelExecution in Test := false

scalaVersion := "2.11.7"

name := "token-bucket"
version := "1.0-SNAPSHOT"
scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.11.7",
  "org.scala-lang" % "scala-actors" % "2.11.7",
  "com.typesafe.akka" %% "akka-actor" % "2.4.1",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
  "org.scalacheck" %% "scalacheck" % "1.12.5" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "com.ning" % "async-http-client" % "1.9.31",
  "org.jsoup" % "jsoup" % "1.8.3",
  "com.github.etaty" %% "rediscala" % "1.6.0-SNAPSHOT"
)
