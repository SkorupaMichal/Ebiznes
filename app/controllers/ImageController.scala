package controllers
import javax.inject._
import play.api.mvc._

@Singleton
class ImageController @Inject()(cc:ControllerComponents) extends AbstractController(cc){

  def getImages = Action{
    Ok("Comments" )
  }

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
