name := "KickerLiga"
 
version := "1.0" 
      
lazy val `kickerliga` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "org.postgresql" % "postgresql" % "42.2.5",
  "org.playframework.anorm" %% "anorm" % "2.6.2",
  jdbc,
  evolutions,
  ehcache,
  ws,
  specs2 % Test,
  guice)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

      