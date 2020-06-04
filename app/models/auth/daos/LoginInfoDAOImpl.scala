package models.auth.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import models.auth.User
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}


class LoginInfoDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider
                                )(implicit ec: ExecutionContext) extends LoginInfoDAO with DAOSlick {

  import profile.api._

  override def saveUserLoginInfo(userID: UUID, loginInfo: LoginInfo): Future[Unit] = {

    val dbLoginInfo = DBLoginInfo(None, loginInfo.providerID, loginInfo.providerKey)

    val loginInfoAction = {
      val retrieveLoginInfo = slickLoginInfos.filter(
        info => info.providerID === loginInfo.providerID &&
          info.providerKey === loginInfo.providerKey).result.headOption
      val insertLoginInfo = slickLoginInfos.returning(slickLoginInfos.map(_.id)).
        into((info, id) => info.copy(id = Some(id))) += dbLoginInfo
      for {
        loginInfoOption <- retrieveLoginInfo
        dbLoginInfo <- loginInfoOption.map(DBIO.successful(_)).getOrElse(insertLoginInfo)
      } yield dbLoginInfo
    }

    val actions = (for {
      dbLoginInfo <- loginInfoAction
      userLoginInfo = DBUserLoginInfo(userID.toString, dbLoginInfo.id.get)
      exists <- existsUserLoginInfo(userLoginInfo)
      _ <- if (exists) DBIO.successful(()) else slickUserLoginInfos += userLoginInfo
    } yield ()).transactionally

    db.run(actions)
  }

  private def existsUserLoginInfo(uli: DBUserLoginInfo) = {
    slickUserLoginInfos.filter(e => e.loginInfoId === uli.loginInfoId && e.userID === uli.userID).exists.result
  }

  def find(userId: UUID, providerId: String): Future[Option[(User, LoginInfo)]] = {
    val action = for {
      ((_, li), u) <- slickUserLoginInfos.filter(_.userID === userId.toString)
        .join(slickLoginInfos).on(_.loginInfoId === _.id)
        .join(slickUsers).on(_._1.userID === _.id)

      if li.providerID === providerId
    } yield (u, li)

    db.run(action.result.headOption).map(_.map{case (u, li) => (DBUser.toUser(u), DBLoginInfo.toLoginInfo(li))})
  }

  override def getAuthenticationProviders(email: String): Future[Seq[String]] = {
    val action = for {
      ((_, _), li) <- slickUsers.filter(_.email === email)
        .join(slickUserLoginInfos).on(_.id === _.userID)
        .join(slickLoginInfos).on(_._2.loginInfoId === _.id)
    } yield li.providerID

    db.run(action.result)
  }
}
