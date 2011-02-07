package org.fao.geonet

object LocalisedString {
  def apply(en:String,de:String="",fr:String="",it:String=""):LocalisedString = {
    val translations = Map("en" -> en,"de" -> de, "fr" -> fr, "it" -> it)
    LocalisedString(translations.filterNot{_._2.trim.isEmpty})
  }
}
case class LocalisedString(translations:Map[String,String]) {
   def formParams(paramName:String)= translations.map{case (key,value) => paramName+key.toUpperCase -> value} toList
}
