package controllers
import javax.inject._
import play.api.mvc._

@Singleton
class CategoryController @Inject()(cc:ControllerComponents) extends AbstractController(cc) {
  def getCategories = Action{
    Ok("Categories")
  }

  def createCategories = Action{
    Ok("Create categoriess")
  }

  def updateCategory(categoryId:Int) = Action{
    Ok("Update category ")
  }

  def deleteCategory(category:Int) = Action{
    Ok("Delete category")
  }

}
