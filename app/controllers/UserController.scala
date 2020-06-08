package controllers

import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json}
import org.mindrot.jbcrypt.BCrypt

import scala.util.{Failure, Success}
import scala.concurrent.{Await, ExecutionContext, Future, duration}

case class CreateUserForm(login: String, email: String, password: String)

case class UpdateUserForm(id: Int, login: String, email: String, password: String)

@Singleton
class UserController @Inject()(cc: ControllerComponents, dd: MessagesControllerComponents)(implicit ex: ExecutionContext) extends MessagesAbstractController(dd) {
  /*Product set controller, client can make easier set of products*/
  val userForm: Form[CreateUserForm] = Form {
    mapping(
      "login" -> nonEmptyText,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(CreateUserForm.apply)(CreateUserForm.unapply)
  }
  val updateUserForm: Form[UpdateUserForm] = Form {
    mapping(
      "id" -> number,
      "login" -> nonEmptyText,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(UpdateUserForm.apply)(UpdateUserForm.unapply)
  }
}