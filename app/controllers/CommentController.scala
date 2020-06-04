package controllers
import models._
import javax.inject._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import scala.concurrent.{Await, ExecutionContext, Future,duration}
import scala.util.{Failure, Success}

case class CreateCommentForm(title:String,content:String,productId:Int,userId:String)
case class UpdateCommentForm(id:Int,title:String,content:String,productId:Int,userId:String)

@Singleton
class CommentController @Inject() (cc:ControllerComponents,commentRepo:CommentRepository,dd:MessagesControllerComponents,
                                   productRepository:ProductRepository,userRepo:daos.UserDAOImpl)(implicit ex:ExecutionContext)  extends MessagesAbstractController(dd) {
  /*Comment to product controller*/

  val commentForm: Form[CreateCommentForm] = Form{
    mapping(
      "title" ->nonEmptyText,
      "content"->nonEmptyText,
      "productId"->number,
      "userId" ->nonEmptyText
    )(CreateCommentForm.apply)(CreateCommentForm.unapply)
  }
  val updateCommentForm: Form[UpdateCommentForm] = Form{
    mapping(
      "id" -> number,
      "title" ->nonEmptyText,
      "content"->nonEmptyText,
      "productId"->number,
      "userId" ->nonEmptyText
    )(UpdateCommentForm.apply)(UpdateCommentForm.unapply)
  }
  var users: Seq[User] = Seq[User]()
  var products: Seq[Product] = Seq[Product]()
  val BadJSON = "Zla skladnia"
  def getUserSeq = {
    userRepo.list()onComplete {
      case Success(c) => users = c
      case Failure(_) => print("fals")
    }
  }
  def getProductSeq = {
    productRepository.list().onComplete {
      case Success(c) => products = c
      case Failure(_) => print("fail")
    }
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
    getUserSeq
    products.map(prod =>Ok(views.html.commentadd(commentForm,prod,users)))
  }

  def createCommentHandle = Action.async { implicit request =>
    getProductSeq
    getUserSeq
    commentForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.commentadd(errorForm,products,users))
        )
      },
      comment => {
        commentRepo.create(comment.title, comment.content, comment.productId,comment.userId).map { _ =>
          Redirect(routes.CommentController.getComments()).flashing("success" -> "product.created")
        }
      }
    )
  }
  def updateComment(commentid: Int): Action[AnyContent] = Action.async{ implicit request: MessagesRequest[AnyContent] =>
    getUserSeq
    getProductSeq
    val comment = commentRepo.getById(commentid)
    comment.map(b=>{
      val bForm = updateCommentForm.fill(UpdateCommentForm(b.head.id,b.head.title,b.head.content,b.head.productId,b.head.userId))
      Ok(views.html.commentupdate(bForm,products,users))
    })
  }
  def updateCommentHandle = Action.async{implicit request=>
    getProductSeq
    getUserSeq
    updateCommentForm.bindFromRequest.fold(
      errorForm =>{
        Future.successful(
          BadRequest(views.html.commentupdate(errorForm,products,users))
        )
      },
      comment =>{
        commentRepo.update(comment.id,Comment(comment.id,comment.title,comment.content,comment.productId,comment.userId)).map{
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
    Await.result(commentwithproductInfo,duration.Duration.Inf)
    commentwithproductInfo.map(b=>Ok(Json.toJson(b)))
  }
  def getCommentByUserID(userID:String) = Action.async{implicit request =>
    val commentbyuser = commentRepo.getByUser(userID)
    Await.result(commentbyuser,duration.Duration.Inf)
    commentbyuser.map(cbu=>Ok(Json.toJson(cbu)))
  }
  def getCommentFromRequestJson(request:MessagesRequest[JsValue]):(String,String,Int,String) = {
    var title = ""
    var content = ""
    var productId = 0
    var userId = ""
    (request.body \ "title").asOpt[String].map{ t=>
      title = t
    }.getOrElse(BadRequest(BadJSON))
    (request.body \ "content").asOpt[String].map{cont=>
      content = cont
    }.getOrElse(BadRequest(BadJSON))
    (request.body \ "product_id").asOpt[Int].map{prodid=>
      productId = prodid
    }.getOrElse(BadRequest(BadJSON))
    (request.body \ "user_id").asOpt[String].map{usid=>
      userId = usid
    }.getOrElse(BadRequest(BadJSON))

    (title,content,productId,userId)
  }
  def createCommentByJson = Action(parse.json){implicit request=>
    val comment = getCommentFromRequestJson(request)
    commentRepo.create(comment._1,comment._2,comment._3,comment._4)
    Ok("")
  }
  def updateCommentByJson(commentId:Int) =Action(parse.json) { implicit request =>
    val comment = getCommentFromRequestJson(request)
    commentRepo.update(commentId, Comment(commentId, comment._1,comment._2,comment._3,comment._4))
    Ok("")
  }
  def deleteCommentByJson(commentId:Int) = Action {
    Await.result(commentRepo.delete(commentId),duration.Duration.Inf)
    Ok
  }

}
