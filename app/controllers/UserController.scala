package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
case class CreateUserForm(login:String,email:String,password:String)
case class UpdateUserForm(id:Int,login:String,email:String,password:String)

@Singleton
class UserController @Inject() (cc:ControllerComponents,dd:MessagesControllerComponents,userRepo:UserRepository)(implicit ex:ExecutionContext) extends MessagesAbstractController(dd){
  /*Product set controller, client can make easier set of products*/
  val userForm: Form[CreateUserForm] = Form{
    mapping(
      "login" -> nonEmptyText,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(CreateUserForm.apply)(CreateUserForm.unapply)
  }
  val updateUserForm: Form[UpdateUserForm] = Form{
    mapping(
      "id" -> number,
      "login" -> nonEmptyText,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(UpdateUserForm.apply)(UpdateUserForm.unapply)
  }



  def getUsers = Action.async{ implicit request=>
    userRepo.list().map(
      ps => Ok(views.html.users(ps))
    )
    //Ok("ProductSets" )
  }
  def getUserById(psId:Int) = Action.async{ implicit request =>
    userRepo.getById(psId).map(
      ps=>ps match{
        case Some(i) => Ok(Json.toJson(i))
        case None => Ok("Brak zbioru prodoktow o podanym id")
      }
    )

  }

  def createUser = Action.async{implicit request =>
    userForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.useradd(errorForm)))
      },
      user =>{
        userRepo.create(user.login,user.email,user.password).map(_=>
          Redirect(routes.UserController.getUsers()).flashing("success"->"basket.created")
        )
      }
    )
  }

  def updateUser(userId: Int) :Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    val user = userRepo.getById(userId)
    user.map(b=>{
      val bForm = updateUserForm.fill(UpdateUserForm(b.head.id,b.head.login,b.head.email
        ,b.head.password))
      Ok(views.html.userupdate(bForm))
    })
  }
  def updateUserHandle =  Action.async{implicit request=>
    updateUserForm.bindFromRequest.fold(
      errorForm =>{
        Future.successful(
          BadRequest(views.html.userupdate(errorForm))
        )
      },
      user =>{
        userRepo.update(user.id,User(user.id,user.login,user.email,user.password)).map{
          _ => Redirect(routes.UserController.updateUser(user.id)).flashing("success"->"basket update")
        }
      }
    )
  }

  def deleteUser(userId: Int) = Action{
    userRepo.delete(userId)
    Redirect("/users")
  }

}