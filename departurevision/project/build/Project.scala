import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info)
{
  lazy val hi = task { println("Hello World"); None }

  val dispatch_vers = "0.8.0.Beta3-SNAPSHOT"
	
  lazy val dispatch_http = "net.databinder" %% "dispatch-http" % dispatch_vers
}