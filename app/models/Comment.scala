package models

import play.api.libs.json.Json

case class Comment(id:Int,title:String,content:String,productId:Int,userId:Int)
object Comment{
  implicit val commentForm = Json.format[Comment]
}