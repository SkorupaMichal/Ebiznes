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
        val password =BCrypt.hashpw(user.password,BCrypt.gensalt())
        userRepo.create(user.login,user.email,password).map(_=>
          Redirect(routes.UserController.getUsers()).flashing("success"->"basket.created")
        )
      }
    )
  }
  def getUserFromLogin(request:MessagesRequest[JsValue]):(String,String) = {
    var login = ""
    var password = ""
    (request.body \ "login").asOpt[String].map{log=>
      login = log
    }
    (request.body \ "password").asOpt[String].map{pass=>
      password = pass
    }
    (login,password)

  }
  def authUserJson  =Action.async(parse.json){implicit request=>
    var user = getUserFromLogin(request);
    println(user)
    var userbase = userRepo.getByLogin(user._1)
    Await.result(userbase,duration.Duration.Inf)
    userbase.map(b=>{
      if(b == None)
        Ok(Json.toJson("Null"))
      else if(BCrypt.checkpw(user._2,b.head.password))
        Ok(Json.toJson(b))
      else
        Ok(Json.toJson("Null"))
    })
    }
  def checkUserIfExists = Action.async(parse.json){implicit request=>
    var login = ""
    (request.body \ "login").asOpt[String].map{log=>
      login = log
    }
    var usercount = userRepo.checkUserLogin(login)
    Await.result(usercount,duration.Duration.Inf)
    usercount.map(b=>Ok(Json.toJson(b)))
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
    var pass = ""
    (request.body \ "login").asOpt[String].map{log=>
      login = log
    }
    (request.body \ "email").asOpt[String].map{ema=>
      email = ema
    }
    (request.body \ "password").asOpt[String].map{passw=>
      pass = passw
    }
    (login,email,pass)

  }
  def createUserByJson = Action(parse.json){implicit request=>
    /*id:Int,login:String,email:String,password:String*/
    val user = getUserFromRequest(request)
    val hashpass = BCrypt.hashpw(user._3,BCrypt.gensalt())
    userRepo.create(user._1,user._2,hashpass)
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