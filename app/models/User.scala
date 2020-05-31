package models

import java.util.UUID

import com.mohiva.play.silhouette.api.Identity
import play.api.libs.json.Json
import models.UserRoles

case class User(id:String, firstName:Option[String], lastName:Option[String], email:Option[String],  avatarUrl:Option[String], role:UserRoles.UserRole) extends Identity

object User{
  implicit val UserForm = Json.format[User]
}
