package c2c.webspecs
package geonetwork
package edit

import c2c.webspecs.BasicValueFactory
import c2c.webspecs.BasicHttpValue

object EditValueFactory extends ValueFactory[IdValue, EditValue] {
  val self = this
  def fromCreateMd() = new ValueFactory[Any,EditValue] {
      def createValue[A <: Any, B >: EditValue]
		  (request:Request[A,B],
		   in:Any, 
		   rawValue:BasicHttpValue,
		   executionContext:ExecutionContext) = {
        val mdId = (rawValue.toXmlValue.getXml \\ "request" \ "id").text.trim
        self.createValue(request,IdValue(mdId,rawValue), rawValue, executionContext)
      }

  }

  def setId(mdId:String) = new ValueFactory[Any,EditValue] {
      def createValue[A <: Any, B >: EditValue]
		  (request:Request[A,B],
		   in:Any, 
		   rawValue:BasicHttpValue,
		   executionContext:ExecutionContext) = self.createValue(request,IdValue(mdId,rawValue), rawValue, executionContext)

  }
    
  
  
  def createValue[A <: IdValue, B >: EditValue]
		  (request:Request[A,B],
		   in:IdValue, 
		   rawValue:BasicHttpValue,
		   executionContext:ExecutionContext) = new EditValue {
    protected def basicValue = rawValue
    lazy val id = in.id
    lazy val version = in.getXml \\ "info" \ "version" text
  }
}

