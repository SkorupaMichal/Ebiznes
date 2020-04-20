package controllers
import models.{SubCategoryRepository,SubCategory,CategoryRepository,Category}
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class CreateSubCategoryForm(name:String,description:String,category_id:Int)
case class UpdateSubCategoryForm(id:Int,name:String,description:String,category_id:Int)
@Singleton
class SubCategoryController @Inject()(cc:ControllerComponents,protected val catRepo:CategoryRepository,dd:MessagesControllerComponents,subcatRepo:SubCategoryRepository)(implicit ex:ExecutionContext) extends MessagesAbstractController(dd){
    /*Sub category controller*/

    val subcategoryForm: Form[CreateSubCategoryForm] = Form{
        mapping(
            "name" -> nonEmptyText,
            "description" ->nonEmptyText,
            "category_id" -> number
        )(CreateSubCategoryForm.apply)(CreateSubCategoryForm.unapply)
    }
    val updateSubCategoryForm: Form[UpdateSubCategoryForm] = Form{
        mapping(
            "id"  -> number,
            "name" -> nonEmptyText,
            "description" ->nonEmptyText,
            "category_id" -> number
        )(UpdateSubCategoryForm.apply)(UpdateSubCategoryForm.unapply)
    }

    def getSubCategories = Action.async{ implicit request =>
        subcatRepo.list().map(
            sc =>Ok(views.html.subcategories(sc))
        )

    }
    def getSubCategorieByID(subcatId:Int) = Action.async{implicit request=>
      subcatRepo.getById(subcatId).map(
          subcat=>subcat match{
              case Some(i) => Ok(Json.toJson(i))
              case None => Ok("Brak podkategori o podanym id")
          }
      )
    }

    def getSubcategory = Action{
        Ok("Subcategory ")
    }
    def index = Action.async { implicit request =>
        val categories = catRepo.list()
        categories.map(cat =>Ok(views.html.subcategoryadd(subcategoryForm,cat)))
    }
    def addSubCategory:Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
        val categories = catRepo.list()
        categories.map(cat =>Ok(views.html.subcategoryadd(subcategoryForm,cat)))
    }
    def addSubCategoryHandle = Action.async { implicit request =>
        var cat:Seq[Category] = Seq[Category]()
        val categories = catRepo.list().onComplete{
            case Success(c) => cat = c
            case Failure(_) =>print("fail")
        }
        subcategoryForm.bindFromRequest.fold(
            errorForm => {
                Future.successful(
                    BadRequest(views.html.subcategoryadd(errorForm,cat))
                )
            },
            subcat => {
                subcatRepo.create(subcat.name, subcat.description, subcat.category_id).map { _ =>
                    Redirect(routes.SubCategoryController.getSubCategories()).flashing("success" -> "product.created")
                }
            }
        )
    }
    def updateSubCategory(subcategoryId:Int): Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
        var cat:Seq[Category] =  Seq[Category]();
        val categories = catRepo.list().onComplete {
            case Success(c) => cat = c
            case Failure(_) => print("fail")
        }
        val subcategory = subcatRepo.getById(subcategoryId)
        subcategory.map(b=>{
            val bForm = updateSubCategoryForm.fill(UpdateSubCategoryForm(b.head.id,b.head.name,b.head.description,b.head.category_id))
            Ok(views.html.subcategoryupdate(bForm,cat))
        })
    }
    def updateSubCategoryHandle = Action.async{implicit request=>
        var cat:Seq[Category] = Seq[Category]()
        val categories = catRepo.list().onComplete{
            case Success(c) => cat = c
            case Failure(_) => print("fail")
        }
        updateSubCategoryForm.bindFromRequest.fold(
            errorForm =>{
                Future.successful(
                    BadRequest(views.html.subcategoryupdate(errorForm,cat))
                )
            },
            subcategory =>{
                subcatRepo.update(subcategory.id,SubCategory(subcategory.id,subcategory.name,subcategory.description,subcategory.category_id)).map{
                    _ => Redirect(routes.SubCategoryController.updateSubCategory(subcategory.id)).flashing("success"->"basket update")
                }
            }
        )
    }
    def deleteSubCategory( subcategoryId:Int) = Action{
        subcatRepo.delete(subcategoryId)
        Redirect("/subcategories")
    }
}
