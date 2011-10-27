package c2c.webspecs
package geonetwork
package geocat
package spec.WP12

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Step
import c2c.webspecs.geonetwork.ImportStyleSheets._
import c2c.webspecs.geonetwork.csw._
import c2c.webspecs.GetRequest
import c2c.webspecs.geonetwork.UserProfiles
import c2c.webspecs.geonetwork.ImportMetadata
import c2c.webspecs.ResourceLoader
import c2c.webspecs.geonetwork.GetRawMetadataXml
import c2c.webspecs.GetRequest


@RunWith(classOf[JUnitRunner]) 
class GM03V2Spec extends GeocatSpecification(UserProfiles.Editor) {
	def is = {
	  "GM03v2 Import / Export test".title 	                                                          ^ Step(setup)            ^
	  	"Imports a GM03 v2 metadata and converts it into iso19139.che for storage into the catalogue" ^ Step(importMetadataId) ^
	  	"Gets the previously inserted MD as GM03v2"                                                   ! getAsGm03v2            ^
	  	"Gets the previously inserted MD as GM03v2Small"                                              ! getAsGm03v2small       ^
	  	"Delete the inserted metadata"							                                      ^ Step(deleteMetadata)   ^
																                                      end ^ Step(tearDown)												
	}
	
	lazy val importMetadataId = {
		val (_,importMd) = ImportMetadata.defaults(uuid, "/geocat/data/metadata.gm03_V2.xml",true, getClass, GeocatImportStyleSheets.GM03_V2)
		val md = (importMd then GetRawMetadataXml).execute().value.getXml
		val response = (md \\ "fileIdentifier").text.trim
		response
	}
	def deleteMetadata = {
		GetRequest("metadata.delete", ("uuid" -> importMetadataId)).execute()
	}
	
	def getAsGm03v2 = {
		val response = GetRequest("gm03.xml", ("uuid" -> importMetadataId)).execute()
		(response.value.getXml \\  "GM03_2Core.Core.MD_Metadata" \ "fileIdentifier").text.trim must beEqualTo (importMetadataId)
	}

	def getAsGm03v2small = {
		val response = GetRequest("gm03small.xml", ("uuid" -> importMetadataId)).execute()
		(response.value.getXml \\   "fileIdentifier").text.trim must beEqualTo (importMetadataId)
	}
	
}