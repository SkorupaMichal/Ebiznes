package controllers
import javax.inject._
import play.api.mvc._

@Singleton
class ProductController @Inject() (cc:ControllerComponents) extends AbstractController(cc) {
  /*Product controller*/

  def getProducts = Action{
    Ok("Product")
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
