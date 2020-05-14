package controllers
import models.{Category, CategoryRepository, ProductRepository, SubCategory, SubCategoryRepository}
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{Await, ExecutionContext, Future, duration}
import scala.util.{Failure, Success}

case class CreateSubCategoryForm(name:String,description:String,categoryId:Int)
case class UpdateSubCategoryForm(id:Int,name:String,description:String,categoryId:Int)
@Singleton
class SubCategoryController @Inject()(cc:ControllerComponents,protected val catRepo:CategoryRepository,dd:MessagesControllerComponents,subcatRepo:SubCategoryRepository,
                                      prodRepo:ProductRepository)(implicit ex:ExecutionContext) extends MessagesAbstractController(dd){
    /*Sub category controller*/

    val subcategoryForm: Form[CreateSubCategoryForm] = Form{
        mapping(
            "name" -> nonEmptyText,
            "description" ->nonEmptyText,
            "categoryId" -> number
        )(CreateSubCategoryForm.apply)(CreateSubCategoryForm.unapply)
    }
    val updateSubCategoryForm: Form[UpdateSubCategoryForm] = Form{
        mapping(
            "id"  -> number,
            "name" -> nonEmptyText,
            "description" ->nonEmptyText,
            "categoryId" -> number
        )(UpdateSubCategoryForm.apply)(UpdateSubCategoryForm.unapply)
    }
    var categories:Seq[Category] = Seq[Category]()

    def getCategoriesSeq = {
        catRepo.list().onComplete{
            case Success(c) => categories = c
            case Failure(_) =>print("fail")
        }
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
        getCategoriesSeq
        subcategoryForm.bindFromRequest.fold(
            errorForm => {
                Future.successful(
                    BadRequest(views.html.subcategoryadd(errorForm,categories))
                )
            },
            subcat => {
                subcatRepo.create(subcat.name, subcat.description, subcat.categoryId).map { _ =>
                    Redirect(routes.SubCategoryController.getSubCategories()).flashing("success" -> "product.created")
                }
            }
        )
    }
    def updateSubCategory(subcategoryId:Int): Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
        getCategoriesSeq
        val subcategory = subcatRepo.getById(subcategoryId)
        subcategory.map(b=>{
            val bForm = updateSubCategoryForm.fill(UpdateSubCategoryForm(b.head.id,b.head.name,b.head.description,b.head.categoryId))
            Ok(views.html.subcategoryupdate(bForm,categories))
        })
    }
    def updateSubCategoryHandle = Action.async{implicit request=>
        getCategoriesSeq
        updateSubCategoryForm.bindFromRequest.fold(
            errorForm =>{
                Future.successful(
                    BadRequest(views.html.subcategoryupdate(errorForm,categories))
                )
            },
            subcategory =>{
                subcatRepo.update(subcategory.id,SubCategory(subcategory.id,subcategory.name,subcategory.description,subcategory.categoryId)).map{
                    _ => Redirect(routes.SubCategoryController.updateSubCategory(subcategory.id)).flashing("success"->"basket update")
                }
            }
        )
    }
    def deleteSubCategory( subcategoryId:Int) = Action{
        subcatRepo.delete(subcategoryId)
        Redirect("/subcategories")
    }

    /*Json api*/
    def getSubcategoriesJson = Action.async{ implicit request =>
        val subcategories = subcatRepo.list()
        Await.result(subcategories,duration.Duration.Inf)
        subcategories.map(b=>Ok(Json.toJson(b)))
    }
    def getSubcategoriesWithCategories = Action.async{implicit request=>
      val catwithsubcat = subcatRepo.getWithCategory
      Await.result(catwithsubcat,duration.Duration.Inf)
      catwithsubcat.map(b=>Ok(Json.toJson(b)))
    }
    def getSubcategoriesByIdJson(subcatId:Int) = Action.async{ implicit request =>
        val subcategories = subcatRepo.getById(subcatId)
        Await.result(subcategories,duration.Duration.Inf)
        subcategories.map(b=>Ok(Json.toJson(b)))
    }
    def getSubcategoriesByCategoryIdJson(categoryId:Int) = Action.async{ implicit request =>
        val subcategories = subcatRepo.getByCategoryId(categoryId)
        Await.result(subcategories,duration.Duration.Inf)
        subcategories.map(b=>Ok(Json.toJson(b)))
    }
    def getSubcategoryFromRequest(request:MessagesRequest[JsValue]) = {
        var name = ""
        var description = ""
        var categoryId = -1
        (request.body \ "name").asOpt[String].map{ i =>
          name = i
        }.getOrElse(BadRequest("Blad"))
        (request.body \ "description").asOpt[String].map{ i =>
            description = i
        }.getOrElse(BadRequest("Blad"))
        (request.body \ "category_id").asOpt[Int].map{ i =>
            categoryId = i
        }.getOrElse(BadRequest("Blad"))
        (name,description,categoryId)
    }
    def createSubCategoryByJson = Action(parse.json){implicit request=>
        /*id:Int,name:String,description:String,category_id:Int*/
        val subcategory = getSubcategoryFromRequest(request)
        subcatRepo.create(subcategory._1,subcategory._2,subcategory._3)
        Ok("")
    }
    def updateSubCategoryByJson(subcategoryId:Int) = Action(parse.json){implicit request=>
        val subcategory = getSubcategoryFromRequest(request)
        subcatRepo.update(subcategoryId,SubCategory(subcategoryId,subcategory._1,subcategory._2,subcategory._3))
        Ok("")
    }
    def deleteSubCategoryByJson(subcategoryId:Int) = Action{
        Await.result(prodRepo.deleteBySubcategory(subcategoryId),duration.Duration.Inf)
        Await.result(subcatRepo.delete(subcategoryId),duration.Duration.Inf)
        Ok("")
    }
}
