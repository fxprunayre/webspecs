package c2c.webspecs
package geonetwork
package geocat

import scala.xml.NodeSeq
import UserProfiles._

abstract class GeocatSpecification(userProfile: UserProfile = Editor) extends GeonetworkSpecification(userProfile) {
	override def extraSetup(setupContext:ExecutionContext):Unit = {
		SharedUserProfile.allNames // ensure SharedUserProfile is loaded
		assert(UserProfiles.all contains SharedUserProfile, "SharedUserProfile has not been loaded");
	  	super.extraSetup(setupContext)
	  
	}
  
	override def extraTeardown(teardownContext:ExecutionContext):Unit = {
	  implicit val currentScopeContext = teardownContext
	  super.extraTeardown(teardownContext)

	  config.adminLogin(None)
	  
	  val newUsers = GeocatListUsers("").value.filter(user => user.username contains uuid.toString)
	  newUsers.foreach(user => DeleteSharedUser(user.userId,true)(None))
	  
	  val newFormats = ListFormats("").value filter (_.name contains uuid.toString)
	  newFormats.foreach(format => DeleteFormat(true)(format.id))
	  
	  val thesauri = GeocatConstants.GEOCAT_THESAURUS :: GeocatConstants.NON_VALIDATED_THESAURUS :: Nil
	  val newKeywords = SearchKeywords(thesauri)(uuid.toString).value
	  newKeywords.foreach(word => DeleteKeyword(word, true)())
	}
	
	
  def hrefInElement(nodeName:String) = 
    (result:Response[NodeSeq]) => {
      val href = (result.value \\ nodeName \@ "xlink:href")
      (href must not beEmpty)
    }
  def hrefHost(nodeName:String) = (result:Response[NodeSeq],s:String) => {
    val href = (result.value \\ nodeName \@ "xlink:href")(0)
    href must startingWith(XLink.PROTOCOL)
  }

}