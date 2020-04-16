package controllers
import models.{ImageRepository}
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ImageController @Inject()(cc:ControllerComponents,reposImages:ImageRepository)(implicit ex:ExecutionContext) extends AbstractController(cc){
  /*Product Image controller*/

  def getImages = Action.async{ implicit request =>
    /* Get all images from database*/
    reposImages.list().map(
      images=>Ok(Json.toJson(images))
    )
    //Ok("Comments" )
  }
  def getImagesById(imageId:Int) = Action.async(implicit request=>{
      reposImages.getById(imageId).map(
        image => image match{
          case Some(i) => Ok(Json.toJson(i))
          case None => Ok("Brak obrazu o podanym id")
        }
      )
  })

  def createImage = Action{
    Ok("Create comment" )
  }

  def updateImage(imageId: Int) = Action{
    Ok("Update comment")
  }

  def deleteImage(imageId: Int) = Action{
    Ok("Delete comment")
  }
}
