package c2c.webspecs
package geonetwork
package csw

object OutputSchemas {
  abstract class  OutputSchema (val name:String) {
    override def toString: String = name
  }
  case object Record extends OutputSchema("csw:Record")
  case object IsoRecord extends OutputSchema("csw:IsoRecord")
  case object GmdNamespace extends OutputSchema("http://www.isotc211.org/2005/gmd")
  case object CheIsoRecord extends OutputSchema("http://www.geocat.ch/2008/che")
  case object GM03Record extends OutputSchema("http://www.isotc211.org/2008/gm03_2")
  
  val DublinCore = Record
}