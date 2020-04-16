package controllers
import models.{ProductRepository}
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductController @Inject() (cc:ControllerComponents,productRepo:ProductRepository)(implicit ex:ExecutionContext) extends AbstractController(cc) {
  /*Product controller*/

  def getProducts = Action.async{ implicit request =>
    productRepo.list().map(
      products=>Ok(Json.toJson(products))
    )
    //Ok("Product")
  }
  def getPRoductByID(productId:Int) = Action.async{ implicit request=>
    productRepo.getById(productId).map(
      product=> product match{
        case Some(i) => Ok(Json.toJson(i))
        case None => Ok("Brak produktu o podanym id")
      }
    )

  }
  def createProduct = Action{
    Ok("Create product")
  }

  def updateProduct(productId: Int) = Action{
    Ok("Update product")
  }

  def deleteProduct(productId: Int) = Action{
    Ok("Delete prodct")
  }
}
