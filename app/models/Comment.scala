package models

import play.api.libs.json.Json

case class Comment(id:Int,title:String,content:String,product_id:Int,user_id:Int)
object Comment{
  implicit val commentForm = Json.format[Comment]
}