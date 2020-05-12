package models

import play.api.libs.json.Json


case class Image(id:Int,url:String,description:String,productId:Int)
object Image{
  implicit val imageForm = Json.format[Image]
}