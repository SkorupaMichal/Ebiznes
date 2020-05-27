package models

import play.api.libs.json.Json


case class User(id:Int,firstName:String,lastName:String,login:String,email:String,password:String)
object User{
  implicit val UserForm = Json.format[User]
}
