package com.gent.departurevision

object DepartureVision {

  /** represents logical unit of data */
  case class Departure(departs: String, train: String, dest: String,
                       line: String, track: String, status: String)

  /** xml extractor */
  case object Extract {
    import xml.{Node, Elem => El, Text}
    def unapply(tup: ((Node, Int), (Node, Int))) = tup match {
      case ((top:Node,_), (bottom:Node,_)) =>
        val Array(departs, train, dest, line)  = top match {
          case El(_, "tr", _, _,
            El(_, "td", _, _,
              El(_, "a", _, _,
                El(_, "font", _, _, Text(departsTrain))
              )
            ),
            El(_, "td", _, _,
              El(_, "font", _, _, Text(dest))
            ),
            El(_, "td", _, _,
              El(_, "font", _, _, Text(line))
            )
         ) => departsTrain.split("/").map(_.replace("&nbsp;","").trim()) ++
              Array(dest.trim(), line.trim())
       }
       val (track, status) = bottom match {
         case El(_,"tr", _, _,
           El(_, "td", _, _,
              El(_, "a", _, _,
                El(_, "font", _, _, Text(track))
              )
           ),
           El(_, "td", _, _,
             El(_, "font", _, _, Text(status))
           )
         ) => (track.replace("&nbsp;","").trim(), status.trim())
      }
      Some(Departure(departs, train, dest, line, track, status))
    }
  }

	val svc = "http://dv.njtransit.com/mobile/tid-mobile.aspx?sid=%s"

  /** scape all departures for a station id and return a handler fn */
	def departures[T](id: String)(f: (Seq[Departure] => T)): T =
		table(id) { asXml(_)(scrape(_)(f)) }

  /** normalize and return iterable seq of strings representing table */
  private def table[T](id: String)(f: Iterable[String] => T): T =
    f("<table>" :: (
      ("</table>" :: (
       io.Source.fromURL(svc format id).
       getLines().
       takeWhile(!_.startsWith("</table>")).
       drop(2).
       filter(!_.startsWith("<hr>")).
       toList.
       reverse)
     ).
     reverse))

  /** if exception occurs return empty list */
  private def asXml[T](it: Iterable[String])(f: xml.NodeSeq=>T): T =
    f(try{
       xml.XML.loadString(it.mkString("").replace("&", "&amp;"))
     } catch { case _ => Nil })

  /** logical units of data come in two rows */
  private def scrape[T](data: xml.NodeSeq)(f: Seq[Departure]=> T): T =
    f((((data \\ "tr").zipWithIndex.partition(_._2 % 2 == 0))
      match { case (l1, l2) => l1 zip l2  }) map { _  match {
        case Extract(departure) => departure
      }
   })

}
