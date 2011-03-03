package c2c.webspecs
package geonetwork

import org.apache.http.entity.mime.content._
import scalax.io.Codec

case class Group(name:String,description:String="") {
  def formParams = List(
    "name" -> new StringBody(name,Codec.UTF8.charSet),
    "description" -> new StringBody(description,Codec.UTF8.charSet))
}

object CreateGroup {
  def apply(groupName:String) = new CreateGroup(Group(groupName))
  def apply(groupName:String,description:String) = new CreateGroup( Group(groupName,description)      )
}
case class CreateGroup(group:Group) extends MultiPartFormRequest[Any,XmlValue]("group.update",XmlValueFactory,group.formParams:_*)

case class DeleteGroup(groupId:String,deleteUsers:Boolean)
  extends DeprecatedAbstractGetRequest("group.remove", XmlValueFactory, "id" -> groupId, "users" -> (if(deleteUsers) "delete" else ""))

