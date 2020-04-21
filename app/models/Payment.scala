package models

import play.api.libs.json.Json


case class Payment(id:Int,name:String,description:String)
object Payment{
  implicit val paymentForm = Json.format[Payment]
}