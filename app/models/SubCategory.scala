package models

import play.api.libs.json.Json


case class SubCategory(id:Int,name:String,description:String,category_id:Int)
object SubCategory{
  implicit val subCategoryForm = Json.format[SubCategory]
}
