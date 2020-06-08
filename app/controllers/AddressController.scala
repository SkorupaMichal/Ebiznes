package controllers

import com.google.inject.{Inject, Singleton}
import models.{Address, AddressRepository}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, MessagesAbstractController, MessagesControllerComponents, Request}

import scala.concurrent.{Await, ExecutionContext, Future, duration}

@Singleton
class AddressController @Inject()(cc:ControllerComponents,dd:MessagesControllerComponents,repo:AddressRepository)(implicit  ec:ExecutionContext) extends MessagesAbstractController(dd){

    def getAllAddressesJson: Action[AnyContent] = Action.async{implicit request=>
      val addresses = repo.list()
      Await.result(addresses,duration.Duration.Inf)
      addresses.map(a=>Ok(Json.toJson(a)))
    }
  def getAddressByIdJson(id:Int): Action[AnyContent] = Action.async{ implicit request=>
    val address = repo.getById(id)
    Await.result(address,duration.Duration.Inf)
    address.map(a=>Ok(Json.toJson(a)))
  }
  def getAddressFromRequest(request:JsValue): (String,String,String) ={
    var city = ""
    var street = ""
    var zipCode = ""
    (request \ "city").asOpt[String].map{ desc=>
        city = desc
    }
    (request \ "street").asOpt[String].map{ desc=>
        street = desc
    }
    (request \ "zip_code").asOpt[String].map{ desc=>
      zipCode = desc
    }
    (city,street,zipCode)
  }
  def createAddressJson = Action.async{implicit request:Request[AnyContent]=>
    request.body.asJson match {
      case Some(json) =>{
        val address = getAddressFromRequest(json)
        repo.create(address._1,address._2,address._3).map(a=>Ok(Json.toJson(a)))
      }
      case None => Future.successful(InternalServerError("Error with json"))
    }
  }
  def deleteAddressJson(addressId:Int) = Action{request=>
    Await.result(repo.delete(addressId),duration.Duration.Inf)
    Ok("")
  }
  def updateAddressJson(addresId:Int) = Action(parse.json){implicit request=>
    val address = getAddressFromRequest(request.body)
    Await.result(repo.update(addresId,Address(addresId,address._1,address._2,address._3)),duration.Duration.Inf)
    Ok("")
  }
}
