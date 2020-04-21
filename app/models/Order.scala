package models

import play.api.libs.json.Json



case class Order(id:Int,date:String,cost:Int,deliver_id:Int,user_id:Int,payment_id:Int,basket_id:Int)
object Order{
  implicit val orderForm = Json.format[Order]
}
