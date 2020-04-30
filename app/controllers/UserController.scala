package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import scala.util.{Failure, Success}
import scala.concurrent.{Await, ExecutionContext, Future,duration}
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
  def createUserByJson = Action(parse.json){implicit request=>
    /*id:Int,login:String,email:String,password:String*/
    val login = (request.body \ "login").as[String]
    val email = (request.body \ "email").as[String]
    val password = (request.body \ "password").as[String]
    userRepo.create(login,email,password)
    Ok("")
  }
  def updateUserByJson(userId:Int) = Action(parse.json){implicit request=>
    val login = (request.body \ "login").as[String]
    val email = (request.body \ "email").as[String]
    val password = (request.body \ "password").as[String]
    userRepo.update(userId,User(userId,login,email,password))
    Ok("")
  }
  def deleteUserByJson(userId:Int) = Action{
    Await.result(userRepo.delete(userId),duration.Duration.Inf)
    Ok("")
  }
}