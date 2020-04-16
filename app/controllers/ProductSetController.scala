package controllers
import models.{ProductSetRepository}
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}
@Singleton
class ProductSetController @Inject() (cc:ControllerComponents,productSetRepo:ProductSetRepository)(implicit ex:ExecutionContext) extends AbstractController(cc){
  /*Product set controller, client can make easier set of products*/
  def getProductSets = Action.async{ implicit request=>
    productSetRepo.list().map(
      ps => Ok(Json.toJson(ps))
    )
    //Ok("ProductSets" )
  }
  def getProductSetById(psId:Int) = Action.async{ implicit request =>
    productSetRepo.getById(psId).map(
      ps=>ps match{
        case Some(i) => Ok(Json.toJson(i))
        case None => Ok("Brak zbioru prodoktow o podanym id")
      }
    )

  }

  def createProductSet = Action{
    Ok("Create ProductSets" )
  }

  def updateProductSet(ProductSetId: Int) = Action{
    Ok("Update ProductSets")
  }

  def deleteProductSet(ProductSetId: Int) = Action{
    Ok("Delete ProductSets")
  }

}
