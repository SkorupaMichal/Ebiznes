package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

import scala.concurrent.{Await, ExecutionContext, Future,duration}
import scala.util.{Failure, Success}

case class CreateCommentForm(title:String,content:String,product_id:Int,user_id:Int)
case class UpdateCommentForm(id:Int,title:String,content:String,product_id:Int,user_id:Int)

@Singleton
class CommentController @Inject() (cc:ControllerComponents,commentRepo:CommentRepository,dd:MessagesControllerComponents,
                                   productRepository:ProductRepository,userRepo:UserRepository)(implicit ex:ExecutionContext)  extends MessagesAbstractController(dd) {
  /*Comment to product controller*/

  val commentForm: Form[CreateCommentForm] = Form{
    mapping(
      "title" ->nonEmptyText,
      "content"->nonEmptyText,
      "product_id"->number,
      "user_id" ->number
    )(CreateCommentForm.apply)(CreateCommentForm.unapply)
  }
  val updateCommentForm: Form[UpdateCommentForm] = Form{
    mapping(
      "id" -> number,
      "title" ->nonEmptyText,
      "content"->nonEmptyText,
      "product_id"->number,
      "user_id" ->number
    )(UpdateCommentForm.apply)(UpdateCommentForm.unapply)
  }


  def getComments = Action.async{ implicit request =>
    commentRepo.list().map(
      comment => Ok(views.html.comments(comment))
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

  def createComment:Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    val products = productRepository.list()
    val users = userRepo.list()
    var user:Seq[User] = Seq[User]()
    users.onComplete {
      case Success(c) => user = c
      case Failure(_) => print("fals")
    }
    products.map(prod =>Ok(views.html.commentadd(commentForm,prod,user)))
  }

  def createCommentHandle = Action.async { implicit request =>
    var prod:Seq[Product] = Seq[Product]()
    var users:Seq[User] = Seq[User]()
    val products = productRepository.list().onComplete{
      case Success(c) => prod = c
      case Failure(_) =>print("fail")
    }
    val u = userRepo.list().onComplete{
      case Success(c) => users = c
      case Failure(_) =>print("fail")
    }
    commentForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.commentadd(errorForm,prod,users))
        )
      },
      comment => {
        commentRepo.create(comment.title, comment.content, comment.product_id,comment.user_id).map { _ =>
          Redirect(routes.CommentController.getComments()).flashing("success" -> "product.created")
        }
      }
    )
  }
  def updateComment(commentid: Int): Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    var prod:Seq[Product] =  Seq[Product]();
    var users:Seq[User] = Seq[User]()
    val products = productRepository.list().onComplete {
      case Success(c) => prod = c
      case Failure(_) => print("fail")
    }
    val u = userRepo.list().onComplete{
      case Success(c) => users = c
      case Failure(_) =>print("fail")
    }
    val comment = commentRepo.getById(commentid)
    comment.map(b=>{
      val bForm = updateCommentForm.fill(UpdateCommentForm(b.head.id,b.head.title,b.head.content,b.head.product_id,b.head.user_id))
      Ok(views.html.commentupdate(bForm,prod,users))
    })
  }
  def updateCommentHandle = Action.async{implicit request=>
    var prod:Seq[Product] = Seq[Product]()
    var users:Seq[User] = Seq[User]()
    val products = productRepository.list().onComplete{
      case Success(c) => prod = c
      case Failure(_) => print("fail")
    }
    val u = userRepo.list().onComplete{
      case Success(c) => users = c
      case Failure(_) =>print("fail")
    }
    updateCommentForm.bindFromRequest.fold(
      errorForm =>{
        Future.successful(
          BadRequest(views.html.commentupdate(errorForm,prod,users))
        )
      },
      comment =>{
        commentRepo.update(comment.id,Comment(comment.id,comment.title,comment.content,comment.product_id,comment.user_id)).map{
          _ => Redirect(routes.CommentController.updateComment(comment.id)).flashing("success"->"basket update")
        }
      }
    )
  }

  def deleteComment(commentId: Int) = Action{
    commentRepo.delete(commentId)
    Redirect("/comments")
  }

  /*Json api*/
  def getCommentsJson = Action.async{implicit request =>
    val comments = commentRepo.list()
    Await.result(comments,duration.Duration.Inf)
    comments.map(b=>Ok(Json.toJson(b)))
  }
  def getCommentByIdJson(commentId:Int) = Action.async{implicit request =>
    val comment = commentRepo.getById(commentId)
    Await.result(comment,duration.Duration.Inf)
    comment.map(b=>Ok(Json.toJson(b)))
  }
  def getCommentByProductID(productid:Int) = Action.async{ implicit  request=>
    val comments = commentRepo.getByProduct(productid)
    Await.result(comments,duration.Duration.Inf)
    comments.map(b=>Ok(Json.toJson(b)))
  }
  def getCommentWithProductDescJson(productId:Int) = Action.async{implicit request=>
    val commentwithproductInfo = commentRepo.getWithProductDesc(productId)
    commentwithproductInfo.map(b=>Ok(Json.toJson(b)))
  }
  def createCommentByJson = Action(parse.json){implicit request=>
    val title = (request.body \ "title").as[String]
    val content = (request.body \ "content").as[String]
    val product_id = (request.body \ "product_id").as[Int]
    val user_id = (request.body \ "user_id").as[Int]
    commentRepo.create(title,content,product_id,user_id)
    Ok("")
  }
  def updateCommentByJson(commentId:Int) =Action(parse.json) { implicit request =>
    val title = (request.body \ "title").as[String]
    val content = (request.body \ "content").as[String]
    val product_id = (request.body \ "product_id").as[Int]
    val user_id = (request.body \ "user_id").as[Int]
    commentRepo.update(commentId, Comment(commentId, title, content, product_id, user_id))
    Ok("")
  }
  def deleteCommentByJson(commentId:Int) = Action {
    Await.result(commentRepo.delete(commentId),duration.Duration.Inf)
    Ok
  }

}
