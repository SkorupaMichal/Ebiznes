package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
@Singleton
class AddressRepository @Inject()(dbConfigProvider:DatabaseConfigProvider)(
                                 implicit executionContext: ExecutionContext){
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._
  class AddressTableDef(tag: Tag) extends Table[Address](tag,"Address"){

    def id = column[Int]("id",O.PrimaryKey,O.AutoInc)
    def city = column[String]("city")
    def street = column[String]("street")
    def zipCode = column[String]("zip_code")
    def * = (id,city,street,zipCode) <> (( Address.apply _ ).tupled, Address.unapply)
  }
  val addresses = TableQuery[AddressTableDef]
  def list():Future[Seq[Address]] = db.run{
    addresses.result
  }
  def getById(id:Int):Future[Option[Address]] = db.run{
    addresses.filter(_.id === id).result.headOption
  }
  def create(city:String,street:String,zipCode:String):Future[Address] = db.run{
    (addresses.map(a=>(a.city,a.street,a.zipCode))
      returning addresses.map(_.id)
      into {case ((city,street,zipCode),id)=>Address(id,city,street,zipCode)}) += (city,street,zipCode)
  }
  def delete(addressId:Int):Future[Unit] = {
    db.run(addresses.filter(_.id === addressId).delete).map(_=>())
  }
  def update(addressId:Int,newAddress:Address):Future[Unit] = {
    val updateAddress: Address = newAddress.copy(addressId)
    db.run(addresses.filter(_.id === addressId).update(updateAddress)).map(_=>())
  }
}
