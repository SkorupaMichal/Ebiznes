package models

import play.api.libs.json.Json

case class Category(id:Int,name:String,description:String)
object Category{
  implicit val categoryForm = Json.format[Category]
}
