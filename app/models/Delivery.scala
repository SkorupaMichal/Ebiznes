package models

import play.api.libs.json.Json


case class Delivery(id:Int,name:String,cost:Int,description: String)
object Delivery{
  implicit val deliveryForm = Json.format[Delivery]
}