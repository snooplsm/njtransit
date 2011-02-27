package com.gent.departurevision

import scala.xml._
import java.net.{URLConnection, URL}
import java.io.{BufferedReader,InputStream,IOException,InputStreamReader}
import java.util.{HashSet}

class DepartureVision {

	val nj = "http://dv.njtransit.com/mobile/tid-mobile.aspx?sid=";

	def departures(id:String):Elem = {
		val url = new java.net.URL(nj + id)
		toDepartures(convertStream(url.openConnection.getInputStream))
	}
	
	private lazy val tableEnd = "</table>"
	
	private def toDepartures(html:StringBuilder):Elem = {
		val tableStartIndex = html.indexOf("<table");
		if(tableStartIndex==(-1)) {
			return null
		}
		val tableEndIndex = html.indexOf(tableEnd,tableStartIndex) + tableEnd.length		
		XML.loadString(html.substring(tableStartIndex,tableEndIndex))
	}

	private val DEPARTS = "DEPARTS"
	private val TO = "TO"
	private val TRK = "TRK"
	private val LINE = "LINE"
	private val TRAIN = "TRAIN"	
	private val STATUS = "STATUS"	

	def filterDepartures(feed:Elem, feedId:String):HashSet[Departure] = {
	   var foundHeader = false
	   var departPos = -1
	   var toPos = -1
	   var trkPos = -1
	   var linePos = -1
	   var trainPos = -1
	   var statusPos = -1
	   var departures = new HashSet[Departure]
	   feed \\ "tr" foreach { (tr:Node) =>
	     if (!isHeader(tr)) {
			if(!foundHeader) {
				departPos = tr \\ "td" findIndexOf {_.text == DEPARTS}
 				toPos = tr \\ "td" findIndexOf {_.text == TO}
				trkPos = tr \\ "td" findIndexOf {_.text == TRK}
				linePos = tr \\ "td" findIndexOf {_.text == LINE}
				trainPos =  tr \\ "td" findIndexOf {_.text == TRAIN}
				statusPos = tr \\ "td" findIndexOf {_.text == STATUS}
				foundHeader = true
			} else {
				var departure = new Departure(null,null,null,null,null,null)	
								
				(tr \\ "td").view.zipWithIndex foreach { e => 
					e._2 match {
						case departPos => println("e")
					}
/*					e._2 match {
						case departPos => departure.departs = e._1.text
						case linePos => departure.line = e._1.text
						case trainPos => departure.train = e._1.text
						case statusPos => departure.status = e._1.text
						case toPos => departure.to = e._1.text
						case trkPos => departure.track = e._1.text
						case _ => println("no dice")
					}
*/				
 				}
		 		//departures.add(departure)
			}
	     }
	   }
		return departures
	 }
	
	def isHeader(p:Node):Boolean = {
		 (p \\ "@colspan" == (5)) 
	 }
	
	private def convertStream(is: InputStream):StringBuilder = {
	    val reader = new BufferedReader(new InputStreamReader(is));
	    val sb = new StringBuilder();

	    var line : String = null
	    try {
	      while ({line = reader.readLine();  line!= null}) {
	        sb.append(line + "\n")
	      }
	    } finally {
	      	try {
	        	is.close()
	      	} catch {
	        	case e: IOException => e.printStackTrace()
	      	}
	    }
		return sb
	  }
}