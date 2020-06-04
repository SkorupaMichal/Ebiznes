package models.auth

import com.mohiva.play.silhouette.api.Identity

case class User(userID: String,
                firstName: Option[String],
                lastName: Option[String],
                email: Option[String],
                avatarURL: Option[String],
                role: UserRoles.UserRole) extends Identity
