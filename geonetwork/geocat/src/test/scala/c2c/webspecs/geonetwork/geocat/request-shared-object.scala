package c2c.webspecs
package geonetwork
package geocat

import java.net.URL
import collection.SeqProxy
import xml.{NodeSeq, Node}
import org.apache.http.entity.mime.content.StringBody
import java.nio.charset.Charset

object SharedObjectTypes extends Enumeration {
  type SharedObjectType = Value
  val contacts, extents, formats, keywords, deleted = Value
}
import SharedObjectTypes._

case class SharedObject(id:Int,
                        url:Option[URL],
                        description:String,
                        objType:SharedObjectType)
class SharedObjectList(val basicValue:BasicHttpValue,
                       val self:List[SharedObject]) extends SeqProxy[SharedObject] with XmlValue

class SharedObjectListFactory(objType:SharedObjectType) extends BasicValueFactory[SharedObjectList] {
  override def createValue(rawValue: BasicHttpValue) = {
    val xmlValue = XmlValueFactory.createValue(rawValue)
    val list = xmlValue.withXml{
      xml =>
        (xml \\ "record").toList map {
          record =>
            val id = (record \\ "id" text).toInt
            val url = {
              val urlTag = record \\ "url"
              if(urlTag.isEmpty) None
              else Some(new URL(record \\ "url" text))
            }
            val desc = (record \\ "desc" text)
            SharedObject(id,url,desc,objType)
        }
    }
    new SharedObjectList(rawValue,list)

  }
}

case class ListNonValidated(sharedType:SharedObjectType)
  extends AbstractGetRequest[Any,SharedObjectList]("reusable.non_validated.list", new SharedObjectListFactory(sharedType), P("type", sharedType.toString))
case object ListNonValidatedContacts
  extends AbstractGetRequest[Any,SharedObjectList]("reusable.non_validated.list", new SharedObjectListFactory(contacts), P("type", contacts.toString))
case object ListNonValidatedFormats
  extends AbstractGetRequest[Any,SharedObjectList]("reusable.non_validated.list", new SharedObjectListFactory(formats), P("type", formats.toString))
case object ListNonValidatedExtents
  extends AbstractGetRequest[Any,SharedObjectList]("reusable.non_validated.list", new SharedObjectListFactory(extents), P("type", extents.toString))
case object ListNonValidatedKeywords
  extends AbstractGetRequest[Any,SharedObjectList]("reusable.non_validated.list", new SharedObjectListFactory(keywords),P("type", keywords.toString))
case object ListDeletedSharedObjects
  extends AbstractGetRequest[Any,SharedObjectList]("reusable.non_validated.list", new SharedObjectListFactory(deleted), P("type", deleted.toString))

case class ReferencingMetadata(mdId:Int,title:String,ownerName:String,email:String)

class ReferencingMetadataList(val basicValue:BasicHttpValue,
                              val self:List[ReferencingMetadata]) extends SeqProxy[ReferencingMetadata] with XmlValue

object ReferencingMetadataListFactory extends BasicValueFactory[ReferencingMetadataList] {

  override def createValue(rawValue: BasicHttpValue) = {
    val xmlValue = XmlValueFactory.createValue(rawValue)
    val list = xmlValue.withXml {
      xml =>
        (xml \\ "record").toList map {
          result =>
            val id = (result \\ "id" text).toInt
            val title = (result \\ "title" text)
            val ownerName = (result \\ "name" text)
            val email = (result \\ "email" text)
            ReferencingMetadata(id,title,ownerName, email)
      }
    }

    new ReferencingMetadataList(rawValue,list)
  }
}
case class ListReferencingMetadata(sharedObjectId:Int, sharedType:SharedObjectType)
  extends AbstractGetRequest[Any,ReferencingMetadataList](
    "reusable.references",
    ReferencingMetadataListFactory,
    P("id", sharedObjectId.toString),
    P("type", sharedType.toString)
  )

case class RejectNonValidatedObject(sharedObjectId:String,
                                    sharedType:SharedObjectType,
                                    rejectionMessage:String="This is a test script rejecting your object, if this is a mistake please inform the system administrators")
  extends AbstractGetRequest[Any,IdValue](
    "reusable.reject",
    ExplicitIdValueFactory(sharedObjectId),
    P("id", sharedObjectId.toString),
    P("type", sharedType.toString),
      P("msg", rejectionMessage)
  )
case class DeleteSharedObject(sharedObjectId:String, sharedType:SharedObjectType)
  extends AbstractGetRequest[Any,IdValue](
    "reusable.delete",
    ExplicitIdValueFactory(sharedObjectId),
    P("id", sharedObjectId.toString),
    P("type", sharedType.toString)
  )
case class ValidateSharedObject(sharedObjectId:String, sharedType:SharedObjectType)
  extends AbstractGetRequest[Any,IdValue](
    "reusable.validate",
    ExplicitIdValueFactory(sharedObjectId),
    P("id", sharedObjectId.toString),
    P("type", sharedType.toString))

case class ProcessSharedObject(xmlData:Node, addOnly:Boolean=false,defaultLang:String="EN")
  extends AbstractMultiPartFormRequest[Any,NodeSeq](
    "reusable.object.process",
    SelfValueFactory[Any,NodeSeq](),
    P("xml", new StringBody(xmlData.toString,"text/xml",Charset.forName("UTF-8"))),
    P("addOnly", new StringBody(addOnly.toString)),
    P(defaultLang, new StringBody("EN"))
  )
  with BasicValueFactory[NodeSeq]{
  def createValue(rawValue: BasicHttpValue): NodeSeq = rawValue.toXmlValue.withXml(x => x)

}
case class UpdateSharedObject(xmlData:Node,defaultLang:String="EN")
  extends AbstractMultiPartFormRequest[Any,NodeSeq](
    "reusable.object.update",
    SelfValueFactory[Any,NodeSeq](),
    P("xml", new StringBody(xmlData.toString,"text/xml",Charset.forName("UTF-8"))),
    P("addOnly", new StringBody("false")),
    P(defaultLang, new StringBody("EN"))
  )
  with BasicValueFactory[NodeSeq]{
  def createValue(rawValue: BasicHttpValue): NodeSeq = rawValue.toXmlValue.withXml(x => x)

}