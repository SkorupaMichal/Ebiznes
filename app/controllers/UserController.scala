package controllers
import models.{UserRepository}
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}
@Singleton
class UserController @Inject() (cc:ControllerComponents,userRepo:UserRepository)(implicit ex:ExecutionContext) extends AbstractController(cc){
  /*Product set controller, client can make easier set of products*/
  def getUsers = Action.async{ implicit request=>
    userRepo.list().map(
      ps => Ok(Json.toJson(ps))
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

  def createUser = Action{
    Ok("Create User" )
  }

  def updateUser(ProductSetId: Int) = Action{
    Ok("Update User")
  }

  def deleteUser(ProductSetId: Int) = Action{
    Ok("Delete User")
  }

}