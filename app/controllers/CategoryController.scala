package controllers
import com.mohiva.play.silhouette.api.Silhouette
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue,JsSuccess, Json}
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import scala.concurrent._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}
import utils.auth._
case class CreateCategoryForm(name:String,description:String)
case class UpdateCategoryForm(id:Int,name:String,description:String)

@Singleton
class CategoryController @Inject()(cc:ControllerComponents,dd:MessagesControllerComponents,repo:CategoryRepository,subcatrepo:SubCategoryRepository,
                                   prodRepo:ProductRepository,silhouette:Silhouette[DefaultEnv])(implicit ex:ExecutionContext) extends MessagesAbstractController(dd) {

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
  def cascadeDeleteCategory(categoryId:Int) = {
    Await.result(prodRepo.deleteByCategory(categoryId),duration.Duration.Inf)
    Await.result(subcatrepo.deleteByCategoryId(categoryId),duration.Duration.Inf)
    Await.result(repo.delete(categoryId),duration.Duration.Inf)
  }
  def deleteCategory(category:Int) = Action{
    cascadeDeleteCategory(category)
    Redirect("/categories")
  }

  /*Json api*/
  def getCategoryJson = Action.async {implicit request =>
    val categories = repo.list()
    Await.result(categories,duration.Duration.Inf)
    categories.map(b=>Ok(Json.toJson(b)))
  }
  def getCategoryByIdJson(categoryId:Int) = Action.async{ implicit request =>
    val categories = repo.getById(categoryId)
    Await.result(categories,duration.Duration.Inf)
    categories.map(b=>Ok(Json.toJson(b)))
  }
  def getCategoryFromRequestJson(request:MessagesRequest[JsValue]):(String,String) = {
    var name = ""
    var description = ""
    (request.body \ "name").asOpt[String].map{ desc=>
      name = desc
    }.getOrElse(BadRequest("Oho zly json"))
    (request.body \ "description").asOpt[String].map{usid=>
      description = usid
    }.getOrElse(BadRequest("Zla skladnia"))

    (name,description)
  }
  def createCategoryByJson = silhouette.SecuredAction(HasRole(UserRoles.Admin)).async(parse.json){implicit request: SecuredRequest[DefaultEnv, JsValue]=>
    
    request.body.validate[Category] match {
      case JsSuccess(json, _) => repo.create(json.name,json.description).map(_=>Ok("Category Createg"))
      case _ => Future.successful(InternalServerError("Bad json"))
    }
  }
  def updateCategoryByJson(categoryId:Int) = Action(parse.json){implicit request=>
    val categoryFromJson = getCategoryFromRequestJson(request)
    repo.update(categoryId,Category(categoryId,categoryFromJson._1,categoryFromJson._2))
    Ok("")
  }
  def deleteCategoryByJson(categoryId:Int) = Action{
    cascadeDeleteCategory(categoryId)
    Ok("")
  }
}
