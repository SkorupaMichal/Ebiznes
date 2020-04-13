package controllers
import javax.inject._
import play.api.mvc._

@Singleton
class ProductSetController @Inject() (cc:ControllerComponents) extends AbstractController(cc){

  def getProductSets = Action{
    Ok("ProductSets" )
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
