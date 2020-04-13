package controllers
import javax.inject._
import play.api.mvc._

@Singleton
class CommentController @Inject() (cc:ControllerComponents) extends AbstractController(cc) {

  def getComments(productId: Int) = Action{
    Ok("Comments" + productId)
  }

  def createComment(productId: Int) = Action{
    Ok("Create comment" + productId)
  }

  def updateComment(productId: Int) = Action{
    Ok("Update comment")
  }

  def deleteComment(productId: Int) = Action{
    Ok("Delete comment")
  }

}
