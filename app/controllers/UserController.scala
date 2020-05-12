package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json}
import com.github.t3hnar.bcrypt._

import scala.util.{Failure, Success}
import scala.concurrent.{Await, ExecutionContext, Future, duration}
case class CreateUserForm(login:String,email:String,password:String)
case class UpdateUserForm(id:Int,login:String,email:String,password:String)

@Singleton
class UserController @Inject() (cc:ControllerComponents,dd:MessagesControllerComponents,userRepo:UserRepository,
                                productbasketRepo: ProductBasketRepository,basketRepo: BasketRepository)(implicit ex:ExecutionContext) extends MessagesAbstractController(dd){
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
  }
  def getUserById(psId:Int) = Action.async{ implicit request =>
    userRepo.getById(psId).map(
      ps=>ps match{
        case Some(i) => Ok(Json.toJson(i))
        case None => Ok("Brak zbioru prodoktow o podanym id")
      }
    )

  }
  def getUsersBasket(userId:Int) = Action.async{ implicit request =>
    val baskets = basketRepo.getByUserId(userId)
    Await.result(baskets,duration.Duration.Inf)
    var buu = Seq[Tuple4[Int,Int,String,Int]]()
    productbasketRepo.getFullListOfProductsByUser(userId).onComplete{
      case Success(c) => buu = c
      case Failure(_) => print("dupa")
    }
        userRepo.getById(userId).map(b=>{Ok(views.html.userbaskets(buu))})
  }
  def createUser = Action.async{implicit request =>
    userForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.useradd(errorForm)))
      },
      user =>{
        val salt = generateSalt
        val password = user.password.bcrypt(salt)
        userRepo.create(user.login,user.email,password).map(_=>
          Redirect(routes.UserController.getUsers()).flashing("success"->"basket.created")
        )
      }
    )
  }
  def validate(value: String, hash: String): Boolean = {
    // Validating the hash
    value.isBcryptedSafe(hash) match {
      case Success(result) => {
        result
      }
      case Failure(failure) => {
        // Hash is invalid
        false
      }
    }

  }
  def authUser(login:String,password:String): Boolean ={
  userRepo.getByLogin(login).onComplete {
    case Success(c) => {
      if (validate(password, c.head.password))
        return true
      else
        return false
    }
    case Failure(_) => false
  }
    (false)
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

  /*Json api*/
  def getUsersJson = Action.async{ implicit request =>
    val users = userRepo.list()
    Await.result(users,duration.Duration.Inf)
    users.map(b=>Ok(Json.toJson(b)))
  }
  def getUsersByIdJson(userId:Int) = Action.async{ implicit request =>
    val users = userRepo.getById(userId)
    Await.result(users,duration.Duration.Inf)
    users.map(b=>Ok(Json.toJson(b)))
  }
  def getUsersByLoginNameJson(login:String) = Action.async{ implicit request =>
    val users = userRepo.getByLogin(login)
    Await.result(users,duration.Duration.Inf)
    users.map(b=>Ok(Json.toJson(b)))
  }
  def getUserFromRequest(request:MessagesRequest[JsValue]):(String,String,String) = {
    var login = ""
    var email = ""
    var password = ""
    (request.body \ "login").asOpt[String].map{log=>
      login = log
    }
    (request.body \ "email").asOpt[String].map{ema=>
      email = ema
    }
    (request.body \ "password").asOpt[String].map{pass=>
      password = pass
    }
    (login,email,password)

  }
  def createUserByJson = Action(parse.json){implicit request=>
    /*id:Int,login:String,email:String,password:String*/
    val user = getUserFromRequest(request)
    userRepo.create(user._1,user._2,user._3)
    Ok("")
  }
  def updateUserByJson(userId:Int) = Action(parse.json){implicit request=>
    val user = getUserFromRequest(request)
    userRepo.update(userId,User(userId,user._1,user._2,user._3))
    Ok("")
  }
  def deleteUserByJson(userId:Int) = Action{
    Await.result(userRepo.delete(userId),duration.Duration.Inf)
    Ok("")
  }
}