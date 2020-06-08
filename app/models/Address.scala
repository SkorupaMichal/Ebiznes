package models

import play.api.libs.json.Json
case class Address(id:Int, city:String, street:String, zip_code:String)
object Address{
  implicit val address = Json.format[Address]
}
