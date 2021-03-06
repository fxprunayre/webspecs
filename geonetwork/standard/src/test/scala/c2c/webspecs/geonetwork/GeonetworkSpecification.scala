package c2c.webspecs
package geonetwork


import org.specs2._
import specification._
import UserProfiles._
import c2c.webspecs.ExecutionContext
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import scala.collection.mutable.SynchronizedQueue
import csw._

@RunWith(classOf[JUnitRunner]) 
abstract class GeonetworkSpecification(userProfile: UserProfile = Editor) extends WebSpecsSpecification[GeonetConfig] {
  implicit val config = GeonetConfig(userProfile, getClass().getSimpleName)
  lazy val UserLogin = config.login

  object template extends Given[String] {
    def extract(text: String): String = extract1(text)
  }
  object randomTemplate extends Given[String] {
    def extract(text: String): String = config.sampleDataTemplateIds(0)
  }

  
  private var mdToDelete = new SynchronizedQueue[Id]()
  
  def registerNewMd(ids:Id*):Unit = mdToDelete ++= ids

  override def extraSetup(setupContext:ExecutionContext) = {
    super.extraSetup(setupContext)

    // don't chain requests because SetSequential is available on all GN instances
    config.adminLogin.assertPassed()
    SetSequentialExecution(true).assertPassed() 
    UserLogin.assertPassed()
  }
  
  override def extraTeardown(tearDownContext:ExecutionContext) = {
    super.extraTeardown(tearDownContext)
    config.adminLogin.execute()

    SetSequentialExecution(false).execute()

    mdToDelete foreach {id => 
      try {DeleteMetadata.execute(id) }
      catch { case _ => println("Error deleting: "+ id) }
    }
  }

  def importMd(numberOfRecords:Int, md:String, identifier:String, styleSheet:ImportStyleSheets.ImportStyleSheet = ImportStyleSheets.NONE) = {
    val replacements = Map("{uuid}" -> identifier)
    val importRequest = ImportMetadata.defaultsWithReplacements(replacements,md,false,getClass,styleSheet)._2
    Thread.sleep(500)
    1 to numberOfRecords map {_ =>
      val id = importRequest.execute().value.id
      registerNewMd(Id(id))
      Id(id)
    }
  }

  def correctResults(numberOfRecords:Int, identifier:String) = (s:String) => {
    val xml = CswGetRecordsRequest(PropertyIsEqualTo("AnyText","Title"+identifier).xml).execute().value.getXml

    (xml \\ "@numberOfRecordsMatched").text.toInt must_== numberOfRecords
  }

  /**
   * delete all metadata.  If adminLogin is true it will login as admin,
   * delete all metadata and the login back in as the user.
   *
   * implicit parameter is present so that the method can be used by tearDown methods
   */
  def deleteAllMetadata(adminLogin:Boolean)(implicit executionContext:ExecutionContext) = {
    if(adminLogin) config.adminLogin.execute()

    var loops = 5
    def search() = XmlSearch(Int.MaxValue).execute()
    while (search().value.records.nonEmpty && loops > 0) {
      (SelectAll then MetadataBatchDelete).execute()
      loops -= 1
    }

    assert(search().value.records.isEmpty, "Unable to delete all records")

    if(adminLogin) UserLogin.execute()
  }

}
