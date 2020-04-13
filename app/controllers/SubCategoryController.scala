package controllers

import javax.inject._
import play.api.mvc._

@Singleton
class SubCategoryController @Inject()(cc:ControllerComponents) extends AbstractController(cc){ 
    
    def getSubCategories = Action{
        Ok("Subcategories")
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
