package c2c.webspecs
package geonetwork
package geocat

import c2c.webspecs.AbstractGetRequest
import java.net.URL
import xml.NodeSeq

object Extents {
  abstract class TypeName(val name:String) {
    override def toString: String = name
  }
  case object NonValidated extends TypeName("gn:non_validated")
  case object Countries extends TypeName("gn:countries")
  case object Validated extends TypeName("gn:gemeindenBB")
  case object Gemeinden extends TypeName("gn:kantoneBB")
  case object Kantone extends TypeName("gn:xlinks")

  val AllTypeNames = Seq(NonValidated, Countries, Validated,Gemeinden, Kantone)

  abstract class Property(val name:String) {
    override def toString: String = name
  }
  case object IdProperty extends Property("id")
  case object DescProperty extends Property("desc")
}

import Extents._
/**
 * Search for an Extent
 */
case class SearchExtent(numResults:Int = 25,
                        property:Property = DescProperty,
                        format:ExtentFormat.Value = ExtentFormat.gmd_bbox,
                        typeName:Seq[Extents.TypeName] = AllTypeNames)
  extends AbstractGetRequest[String,List[ExtentSummary]]("extent.search.list!", SelfValueFactory[String,List[ExtentSummary]],
    IdP("pattern"),
    SP("numResults", numResults),
    SP("property", property),
    SP("typename", typeName mkString ","),
    SP("format", format) )
  with BasicValueFactory[List[ExtentSummary]] {
  def createValue(rawValue: BasicHttpValue): List[ExtentSummary] = {
    rawValue.toXmlValue.withXml{
      xml =>
        (xml \\ "feature").toList map {feature =>
          val id = feature \\ "@id" text
          val href = feature \\ "@href" text
          val desc = parseLanguages(feature \\ "desc")
          val validated = !href.contains("typename=gn:non_validated")
          val fullHref = "http://"+Properties.testServer+href
          ExtentSummary(id,new URL(fullHref), desc,validated)
        }
    }
  }

  private def parseLanguages(node:NodeSeq) = {
    val values = (node \ "_").toList map {n => n.label.toLowerCase -> n.text}
    Localized(Map(values:_*))
  }
}

case class ExtentSummary(id:String, href:URL, desc:Localized, validated:Boolean)

/**
 * Represents the translations of an item
 */
case class Localized(translation:Map[String,String])
object ExtentFormat extends Enumeration {
  val gmd_bbox, gmd_polygon, gmd_complete = Value
}

case class DeleteExtent(typeName:Extents.TypeName, id:String) extends AbstractGetRequest("xml.extent.delete",
    XmlValueFactory,
    SP("typename" -> typeName),
    SP("id" -> id)
  )