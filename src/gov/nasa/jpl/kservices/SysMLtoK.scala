package gov.nasa.jpl.kservices

import org.json.JSONObject;
import org.json.JSONTokener;

object SysMLtoK {
  
}

abstract class TranslationPattern {
  def matches(json: String): Boolean
  def translate(json: String): String
  protected def strToJsonObj(jsonStr: String): JSONObject = {
    new JSONObject(new JSONTokener(jsonStr))
  }
}

case object BlockTr extends TranslationPattern {
  def matches(json: String) = {
    val jsonObj = strToJsonObj(json);
    println(s"DEBUG: ${jsonObj}"); //DEBUG
    true;
  }
  def translate(json: String) = ???
}