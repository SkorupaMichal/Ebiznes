package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}

case class CreateCategoryForm(name:String,description:String)

@Singleton
class CategoryController @Inject()(cc:ControllerComponents,repo:CategoryRepository)(implicit ex:ExecutionContext) extends AbstractController(cc) {

  /*Category controller*/
  val categoryForm: Form[CreateCategoryForm] = Form{
    mapping(
    "name" -> nonEmptyText,
    "description" ->nonEmptyText)(CreateCategoryForm.apply)(CreateCategoryForm.unapply)
  }
  def getCategories = Action.async{ implicit request=>
    repo.list().map(
      category=>Ok(Json.toJson(category))
    )
    //Ok("Categories")
  }
  def getCategoriesByID(categoryId:Int) = Action.async{ implicit request=>
    repo.getById(categoryId).map(
      category=>
        category match{
          case Some(i) => Ok(Json.toJson(i))
          case None => Ok("Brak kaegori o podanym id")
        }
    )
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
