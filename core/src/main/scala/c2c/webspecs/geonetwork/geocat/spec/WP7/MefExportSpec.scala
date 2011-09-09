package c2c.webspecs
package geonetwork
package geocat
package spec.WP7

import java.util.Enumeration
import collection.JavaConverters._
import csw._
import org.specs2.specification.Step
import c2c.webspecs.geonetwork.ImportStyleSheets.NONE
import c2c.webspecs.geonetwork._
import c2c.webspecs.{ XmlValue, Response, IdValue, GetRequest }
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
import java.util.Date
import org.specs2.matcher.ContainMatcher
import org.specs2.control.LazyParameters
import org.specs2.control.LazyParameter
import scala.xml.NodeSeq
import org.specs2.control.LazyParameter
import java.util.zip.ZipFile
import scalax.file.Path
import scalax.file.defaultfs.DefaultPath
import java.io.InputStreamReader
import scalax.io.Resource
import java.util.zip.ZipEntry
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import java.io.File
import java.io.FileOutputStream

@RunWith(classOf[JUnitRunner])
class MefExportSpec extends GeocatSpecification { def is =
    sequential ^ "This spec verifies a fix of a bug where imported data cannot be immediately found" ^ Step(setup) ^
    "Import a metadata"                                                                              ^ Step(importAndGetIds) ^
    "Select imported MD"                                                                             ! select ^
    "Download MEF package"                                                                           ^ Step(download) ^
    "verify there are 2 copies"                                                                      ! twoCopies ^
    "verify the metadata is in iso19139"                                                             ! iso19139 ^
    "verify the metadata is in GM03"                                                                 ! gm03 ^
    "verify the metadata is in iso19139"                                                             ! che ^
                                                                                                       Step(tearDown)
 
  lazy val importAndGetIds = importMd(2)
  def select = {
  // first do a search because that is required for a selection
  val correctSearch = correctResults(2)("")
  GetRequest("metadata.select","id" -> 0, "selected" -> "add-all")()
  val correctSelection = GetRequest("metadata.select","selected" -> "status")().value.getXml.text.trim must_== "2"
  
  correctSearch and correctSelection
}
  lazy val download = {
    val request = new AbstractGetRequest[Any,ZipFile]("mef.export", ZipFileValueFactory, SP("format" -> "full"), SP("version" -> "2")) {}
    request().value
  }

  def entries = download.entries.asInstanceOf[Enumeration[ZipEntry]].asScala.toList

  def twoCopies = {
    entries.filter(_.getName endsWith "metadata-gm03_2.xml") must haveSize(2)
  }

  def iso19139 = {
    val iso = entries.filter(_.getName endsWith "metadata.iso19139.xml")

    iso must haveSize(2)
  }

  def che = {
    val md = entries.filter(_.getName endsWith "metadata.xml")
    md must haveSize(2)
  }

  def gm03 = {
    val md = entries.filter(_.getName endsWith "metadata-gm03_2.xml")
    
    val in = Resource.fromInputStream(download.getInputStream(md(0)))
    val isGM03 = in.lines().find(_.contains("<TRANSFER")).nonEmpty
    isGM03 aka "File is GM03" must beTrue
  }

  
}

object ZipFileValueFactory extends BasicValueFactory[ZipFile] {
  def createValue(rawValue:BasicHttpValue):ZipFile = {
    rawValue.data.fold(throw _, bytes => {
      val path = Path.createTempFile()
      path.write(bytes)
      val jfile = path.asInstanceOf[DefaultPath].jfile
      new ZipFile(jfile)
    })
  }
}