package models


import com.mohiva.play.silhouette.api.Identity

case class User(id:String, firstName:Option[String], lastName:Option[String], email:Option[String],  avatarUrl:Option[String], role:UserRoles.UserRole) extends Identity

