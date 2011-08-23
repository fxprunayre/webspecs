package c2c.webspecs
package geonetwork
package geocat
package spec.WP10

import org.specs2.specification.Step
import edit.AddSites._

class RejectSharedObjectSpec extends AbstractSharedObjectSpec { def is =
  "Reject Shared Object Specification".title           ^ Step(setup) ^ 
  "This specification declares the behaviour of deleting a shared object"                   ^ 
      "First create several shared objects"                                                 ^ Step(CreateNonValidatedObjects) ^
      "Then import a metadata and add the shared objects to that Metadata"                  ^ Step(createMetadata) ^ bt ^
      ("Rejecting the contact via the RejectSharedObject API " +
      		"will move the contact to deleted object table and will update the" +
      		"metadata with the new xlink")                                                  ! rejectContact ^  
	  ("Rejecting the extent via the RejectSharedObject API " +
	        "will move the contact to deleted object table and will update the" +
	        "metadata with the new xlink")                                                  ! rejectExtent ^  
      ("Rejecting the format via the RejectSharedObject API " +
            "will move the contact to deleted object table and will update the" +
            "metadata with the new xlink")                                                  ! rejectFormat ^  
      ("Rejecting the keyword via the RejectSharedObject API " +
            "will move the contact to deleted object table and will update the" +
            "metadata with the new xlink")                                                  ! rejectKeyword ^
                                                                                              Step(tearDown)

  def doReject(obj: SharedObject, link: AddSite) = {
    val rejected = RejectNonValidatedObject(obj.id, SharedObjectTypes.contacts)().value
    val isInRejectedTable = ListDeletedSharedObjects().value.map(_.id) must contain(rejected.id)

    isInRejectedTable and validateCorrectRejection(rejected.id, link)
  }

  def rejectContact = doReject(findSharedContact.get, contact)
  def rejectExtent = doReject(findSharedExtent.get, extent)
  def rejectFormat = doReject(findSharedFormat.get, distributionFormat)
  def rejectKeyword = doReject(findSharedKeyword.get, descriptiveKeywords)

}