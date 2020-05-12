package models

import play.api.libs.json.Json


case class Product(id:Int,name:String,cost:Int,count:Int,producer:String,categoryId:Int,subcategoryId:Int)
object Product{
  implicit val productForm = Json.format[Product]
}