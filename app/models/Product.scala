package models

import play.api.libs.json.Json


case class Product(id:Int,name:String,cost:Int,count:Int,producer:String,subcategory_id:Int)
object Product{
  implicit val productForm = Json.format[Product]
}