package controllers
import models.{SubCategoryRepository}
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}
@Singleton
class SubCategoryController @Inject()(cc:ControllerComponents,subcatRepo:SubCategoryRepository)(implicit ex:ExecutionContext) extends AbstractController(cc){
    /*Sub category controller*/
    def getSubCategories = Action.async{ implicit request =>
        subcatRepo.list().map(
            sc =>Ok(Json.toJson(sc))
        )
        //Ok("Subcategories")
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

    def addSubCategory(categoryId:Int) = Action{
        Ok("Add subcategory")
    }

    def removeSubCategory(categoryId:Int) = Action{
        Ok("Remove Subcategory")
    }
}
