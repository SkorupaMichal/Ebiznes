package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}

case class CreateCategoryForm(name:String,description:String)
case class UpdateCategoryForm(id:Int,name:String,description:String)

@Singleton
class CategoryController @Inject()(cc:ControllerComponents,dd:MessagesControllerComponents,repo:CategoryRepository)(implicit ex:ExecutionContext) extends MessagesAbstractController(dd) {

  /*Category controller*/
  val categoryForm: Form[CreateCategoryForm] = Form{
    mapping(
    "name" -> nonEmptyText,
    "description" ->nonEmptyText)(CreateCategoryForm.apply)(CreateCategoryForm.unapply)
  }
  val updateCategoryForm: Form[UpdateCategoryForm] = Form{
    mapping(
      "id"  -> number,
      "name" -> nonEmptyText,
      "description" ->nonEmptyText)(UpdateCategoryForm.apply)(UpdateCategoryForm.unapply)
  }
  def getCategories = Action.async{ implicit request=>
    repo.list().map(
      category=>Ok(views.html.categories(category))
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
  def index = Action { implicit request =>
    Ok(views.html.categoryadd(categoryForm))

  }
  def createCategories = Action.async{implicit request =>
    categoryForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.categoryadd(errorForm)))
      },
      category =>{
        repo.create(category.name,category.description).map(_=>
          Redirect(routes.CategoryController.getCategories()).flashing("success"->"basket.created")
        )
      }
    )
  }

  def updateCategory(categoryId:Int): Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    val category = repo.getById(categoryId)
    category.map(b=>{
      val bForm = updateCategoryForm.fill(UpdateCategoryForm(b.head.id,b.head.name,b.head.description))
      Ok(views.html.categoryupdate(bForm))
    })
  }
  def updateCategoryHandle = Action.async{implicit request=>
    updateCategoryForm.bindFromRequest.fold(
      errorForm =>{
        Future.successful(
          BadRequest(views.html.categoryupdate(errorForm))
        )
      },
      category =>{
        repo.update(category.id,Category(category.id,category.name,category.description)).map{
          _ => Redirect(routes.CategoryController.updateCategory(category.id)).flashing("success"->"basket update")
        }
      }
    )
  }

  def deleteCategory(category:Int) = Action{
    repo.delete(category);
    Redirect("/categories")
  }

}
