import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) {
  lazy val specs = specsDependency % "test"
  def specsDependency =
    if (buildScalaVersion startsWith "2.7.")
      "org.scala-tools.testing" % "specs" % "1.6.2.2"
    else
      "org.scala-tools.testing" % "specs_2.8.0" % "1.6.5"
}
