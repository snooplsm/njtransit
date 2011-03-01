package com.gent.departurevision

import org.specs._

object DepartureVisionSpec extends Specification {
  "DepartureVision" should {
    "scrape data for valid id" in {
      val departures = DepartureVision.departures("NY") { d =>
       d.foreach(println)
       d
      }
      departures.size must be > 0
    }
    "fail gracefully with invalid id" in {
      val departures = DepartureVision.departures("bogus")(d=>d)
      departures must beEmpty
    }
  }
}
