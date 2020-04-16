package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}
case class CommentCategoryForm(title:String,content:String,product_id:Int)

@Singleton
class CommentController @Inject() (cc:ControllerComponents,commentRepo:CommentRepository,
                                   productRepository:ProductRepository)(implicit ex:ExecutionContext) extends AbstractController(cc) {
  val commentForm: Form[CommentCategoryForm] = Form{
    mapping("title" ->nonEmptyText,
    "content"->nonEmptyText,
    "product_id"->number)(CommentCategoryForm.apply)(CommentCategoryForm.unapply)
  }
  /*Comment to product controller*/

  def getComments = Action.async{ implicit request =>
    commentRepo.list().map(
      comment => Ok(Json.toJson(comment))
    )
  }
  def getCommentsByProductID(productId: Int) = Action.async{ implicit request =>
    /*Return comment by product*/
    commentRepo.getByProduct(productId).map(
      comments => Ok(Json.toJson(comments))
    )
    // Ok("Comments" + productId)
  }
  def getCommentByID(commentId: Int) = Action.async{ implicit  request=>
    commentRepo.getById(commentId).map(
      comment=> comment match{
        case Some(i) => Ok(Json.toJson(i))
        case None => Ok("Brak komentarza o podanym id")
      }
    )
  }
  def getAllComents = Action.async{ implicit request =>
    /*Get all comments in database*/
    commentRepo.list().map(
      comments=>Ok(Json.toJson(comments))
    )
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
