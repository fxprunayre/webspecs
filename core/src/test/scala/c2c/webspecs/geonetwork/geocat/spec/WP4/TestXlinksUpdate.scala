package c2c.webspecs
package geonetwork
package geocat
package spec.WP4

import csw._
import org.specs2.specification.Step
import c2c.webspecs.geonetwork.ImportStyleSheets.NONE
import c2c.webspecs.geonetwork._
import c2c.webspecs.{XmlValue, Response, IdValue, GetRequest}
import accumulating._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import csw._
import scala.xml.transform.BasicTransformer
import c2c.webspecs.geonetwork.geocat.spec.WP3.ProcessImportedMetadataSpec
import scala.xml.Node
import scala.xml.XML
import scala.xml.Elem
import org.specs2.execute.Result


// TODO : finish to implement this test ...


@RunWith(classOf[JUnitRunner]) 
class TestXlinksUpdate extends GeonetworkSpecification(UserProfiles.Editor) {  def is =

  "This specification tests how xlinks are correctly processed"             					  ^ Step(setup)               ^
      "When a ${data} metadata is imported"                                                       ^ importMetadata.toGiven ^
      "The import must complete successfully"                                                     ^ a200ResponseThen.narrow[Response[(Node,Node)]]  ^
      "All ${contact} xlinks must have been resolved and there fore have children"			  	  ^ xlinked.toThen ^
      "All ${descriptiveKeywords} must have been resolved and there fore have children"			  ^ xlinked.toThen ^
      "All ${citedResponsibleParty} must have been resolved and there fore have children"	 	  ^ xlinked.toThen ^
      "All ${pointOfContact} must have been resolved and there fore have children"			  	  ^ xlinked.toThen ^
      "All ${resourceFormat} must have been resolved and there fore have children"			  	  ^ xlinked.toThen ^
      "All ${userContactInfo} must have been resolved and there fore have children"			      ^ xlinked.toThen ^
      "All ${distributionFormat} must have been resolved and there fore have children"			  ^ xlinked.toThen ^
      "All ${distributorContact} must have been resolved and there fore have children"			  ^ xlinked.toThen ^
                                                                                                  Step(tearDown)


                              

   val importMetadata:(String) => Response[(Node,Node)] = (s:String) => {
     val (xmlString,data) = extract1(s) match {
       case "data" =>
          ResourceLoader.loadDataFromClassPath("/geocat/data/comprehensive-iso19139che.xml",classOf[ProcessImportedMetadataSpec],uuid)
       case "service" =>
          ResourceLoader.loadDataFromClassPath("/geocat/data/wfs-service-metadata-template.xml",classOf[GeonetworkSpecification],uuid)
     }
    
     val originalXml = XML.loadString(xmlString)
     val ImportRequest = ImportMetadata.findGroupId(data,NONE,false)
     
     val response = (UserLogin then ImportRequest then GetEditingMetadataXml startTrackingThen DeleteMetadata)(None)

     response._1.map(mv => (originalXml, mv.getXml.asInstanceOf[Node]))
   }
 
   val xlinked = (r:Response[(Node,Node)],s:String) => {
     val (_, importedMd) = r.value
     val node = extract1(s)
     val hrefs = importedMd \\ node filter (_ @@ "xlink:href" nonEmpty)
     
     val haveChildren = (hrefs foldLeft (success:Result)) {(result,next) =>
       result and (next.child must not beEmpty)
     }

     (hrefs must not beEmpty) and haveChildren 
   }
}