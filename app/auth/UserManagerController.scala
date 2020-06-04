package auth

import java.util.UUID

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.Inject
import models.auth.UserRoles
import models.auth.services.UserService
import play.api.data.Form
import play.api.data.Forms.{single, _}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.auth.{DefaultEnv, HasRole}

import scala.concurrent.ExecutionContext

class UserManagerController @Inject()(components: ControllerComponents,
                                      userService: UserService,
                                      silhouette: Silhouette[DefaultEnv])(implicit ec: ExecutionContext) extends AbstractController(components) {

  def changeUserRole(userId: UUID) = silhouette.SecuredAction(HasRole(UserRoles.Admin)).async(parse.form(UserRoleForm.form)) { implicit request =>
    val roleId = request.body
    userService.changeUserRole(userId, UserRoles(roleId)).map(_ => Ok)
  }

  def userRoles() = silhouette.SecuredAction(HasRole(UserRoles.Admin)) {
    Ok(Json.toJson(UserRoles.values.map(v => Json.obj("name" -> v.toString, "id" -> v.id))))
  }
}

object UserRoleForm {
  val form = Form(
    single("roleId" -> number)
  )
}

