package c2c.webspecs
package geonetwork
package geocat
package spec.WP3

import org.specs2.specification.Step
import ImportStyleSheets._
import edit._
import scala.xml.transform.BasicTransformer
import scala.xml.Node
import scala.xml.XML
import scala.xml.Elem
import scala.xml.MetaData
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import scala.xml.NodeSeq
import org.specs2.execute.Result

@RunWith(classOf[JUnitRunner]) 
class ProcessImportedMetadataSpec extends GeocatSpecification { def is =
  "Shared Object Processing of Imported Metadata".title ^ Step(setup) ^
  "This specification tests how imported metadata are processed for shared objects"             ^
  "When a data metadata is imported"                                                            ^ importFragments(importedDataMetadata) ^ p ^
//  "When a service metadata is imported"                                                         ^ importFragments(importedServiceMetadata) ^
  Step(tearDown)

  def importFragments(importRequest: => Response[TestData]) = {
       Step(importRequest) ^
      "The import must complete successfully"                                                   ! importHas200Response(importRequest) ^
      "All ${extent} must have ${several} xlink:href attributes"                                ! xlinked(importRequest) ^
      "All ${contact} must have ${several}  xlink:href attributes"                              ! xlinked(importRequest) ^
      "All ${descriptiveKeywords} must have ${several} xlink:href attributes"                   ! xlinked(importRequest) ^
      "All ${citedResponsibleParty} must have ${several} xlink:href attributes"                 ! xlinked(importRequest) ^
      "All ${parentResponsibleParty} must have ${several} xlink:href attributes"                ! xlinked(importRequest) ^
      "All ${pointOfContact} must have ${several} xlink:href attributes"                        ! xlinked(importRequest) ^
      "All ${resourceFormat} must have ${several} xlink:href attributes"                        ! xlinked(importRequest) ^
      "All ${userContactInfo} must have ${several} xlink:href attributes"                       ! xlinked(importRequest) ^
      "All ${distributionFormat} must have ${several} xlink:href attributes"                    ! xlinked(importRequest) ^
      "All ${distributorContact} must have ${several} xlink:href attributes"                    ! xlinked(importRequest) ^
      "All ${sourceExtent} must have ${1} xlink:href attributes"                                ! xlinked(importRequest) ^
      "All ${spatialExtent} must have ${2} xlink:href attributes"                               ! xlinked(importRequest) ^ p ^
      "Updating a shared contact through the geonetwork edit API"								^ updateContact(importRequest).toGiven ^
      	"Should be present in the metadata the next time the metadata is accessed"				^ newContact.toThen ^ end ^ Step(deleteMd(importRequest))
      
	}
                       
   case class TestData(id:IdValue, mdWithoutXLinks:NodeSeq, mdWithXLinks:NodeSeq)

   def deleteMd(testData: => Response[TestData]) = {
     val deleteResponse = DeleteMetadata(testData.value.id)
     assert(deleteResponse.basicValue.responseCode == 200, "Failed to delete imported metadata")
   }
   def doImport(fileName:String):Response[TestData] = {
     val (xmlString,data) = ResourceLoader.loadDataFromClassPath("/geocat/data/"+fileName,classOf[ProcessImportedMetadataSpec],uuid)
     val originalXml = XML.loadString(xmlString)
     val ImportRequest = ImportMetadata.findGroupId(data,NONE,false)
     
     val importResponse = (UserLogin then ImportRequest)(None)
     val id = importResponse.value
     val mdWithXLinks =  GetEditingMetadataXml(id).value.getXml
     val mdWithoutXLinks = GetRawMetadataXml(id).value.getXml

     importResponse.map(mv => TestData(id,mdWithoutXLinks, mdWithXLinks)) 
	}
   lazy val importedDataMetadata = doImport("comprehensive-iso19139che.xml")
   lazy val importedServiceMetadata = doImport("comprehensive-service-iso19139che.xml")
   
  def importHas200Response(testData: => Response[TestData]) = testData must haveA200ResponseCode
  def xlinked(testData: => Response[TestData]) = (s:String) => {
    val mdWithXLinks = testData.value.mdWithXLinks
    val (node, number) = extract2(s)

    val elem = mdWithXLinks \\ node
    val countMatcher = if (number == "several") {
      (elem \@ "xlink:href" aka "href" must not beEmpty)
    } else {
      (elem \@ "xlink:href" aka "href" must haveSize(number.toInt))
    }

    (elem foldLeft (success: Result)) { (acc, next) => acc and (next.child must not beEmpty) } and
      (elem aka (node + " element") must not beEmpty) and
      countMatcher 
   }

  def updateContact(importRequest: => Response[TestData]) = () => {
    val mdWithXLinks = importRequest.value.mdWithXLinks
    val id = importRequest.value.id
    
    val orgName = mdWithXLinks \\ "CHE_MD_Metadata" \ "contact" \ "CHE_CI_ResponsibleParty" \ "organisationName"
    val e = orgName \\ "LocalisedCharacterString" \ "element"
    val e2 = orgName \\ "LocalisedCharacterString" \\ "element"
    
    val deRef = (orgName \\ "LocalisedCharacterString" \ "element" \@ "ref").head
    val orgRef = (orgName \ "element" \@ "ref").head
    
    val updateDe = "_"+deRef -> newDeOrgName
    val addFr = "_lang_FR_"+orgRef -> newFrOrgName
    val addEn = "_lang_EN_"+orgRef -> newEnOrgName
    val addIt = "_lang_IT_"+orgRef -> newItOrgName
    
    (StartEditing(id.id) then UpdateMetadata(updateDe,addFr,addEn,addIt))(None)

    GetRawMetadataXml(id).value.getXml
  }
  
  val newDeOrgName = "NewDeOrg"
  val newFrOrgName = "NewFrOrg"
  val newEnOrgName = "NewEnOrg"
  val newItOrgName = "NewItOrg"
  def newContact = (md:NodeSeq) => {
    
    val locales = md \\ "CHE_MD_Metadata" \ "contact" \ "CHE_CI_ResponsibleParty" \ "organisationName" \\ "LocalisedCharacterString"
    
    val de = locales find (l => (l @@ "locale").head == "#DE") map (_.text.trim)
    val fr = locales find (l => (l @@ "locale").head == "#FR") map (_.text.trim)
    val en = locales find (l => (l @@ "locale").head == "#EN") map (_.text.trim)
    val it = locales find (l => (l @@ "locale").head == "#IT") map (_.text.trim)
    
    (de must beSome(newDeOrgName)) and
    	(fr must beSome(newFrOrgName)) and
    	(en must beSome(newEnOrgName)) and
    	(it must beSome(newItOrgName))
  }
}