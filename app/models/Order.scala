package models

import play.api.libs.json.Json



case class Order(id:Int,date:String,cost:Int,deliverId:Int,userId:String,paymentId:Int,basketId:Int)
object Order{
  implicit val orderForm = Json.format[Order]
}
