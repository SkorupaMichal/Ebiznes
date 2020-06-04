package models.auth.daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag


class OAuth2InfoDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext, val classTag: ClassTag[OAuth2Info])
  extends DelegableAuthInfoDAO[OAuth2Info] with DAOSlick {

  import profile.api._

  protected def oAuth2InfoQuery(loginInfo: LoginInfo) = for {
    dbLoginInfo <- loginInfoQuery(loginInfo)
    dbOAuth2Info <- slickOAuth2Infos if dbOAuth2Info.loginInfoId === dbLoginInfo.id
  } yield dbOAuth2Info

  protected def oAuth2InfoSubQuery(loginInfo: LoginInfo) =
    slickOAuth2Infos.filter(_.loginInfoId in loginInfoQuery(loginInfo).map(_.id))

  protected def addAction(loginInfo: LoginInfo, authInfo: OAuth2Info) =
    loginInfoQuery(loginInfo).result.head.flatMap { dbLoginInfo =>
      slickOAuth2Infos += DBOAuth2Info(
        None,
        authInfo.accessToken,
        authInfo.tokenType,
        authInfo.expiresIn,
        authInfo.refreshToken,
        dbLoginInfo.id.get)
    }.transactionally

  def updateAction(loginInfo: LoginInfo, authInfo: OAuth2Info) =
    oAuth2InfoSubQuery(loginInfo).
      map(dbOAuth2Info => (dbOAuth2Info.accessToken, dbOAuth2Info.tokenType, dbOAuth2Info.expiresIn, dbOAuth2Info.refreshToken)).
      update((authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken))

  def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = {
    val result = db.run(oAuth2InfoQuery(loginInfo).result.headOption)
    result.map { dbOAuth2InfoOption =>
      dbOAuth2InfoOption.map { dbOAuth2Info =>
        OAuth2Info(dbOAuth2Info.accessToken, dbOAuth2Info.tokenType, dbOAuth2Info.expiresIn, dbOAuth2Info.refreshToken)
      }
    }
  }

  def add(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] =
    db.run(addAction(loginInfo, authInfo)).map(_ => authInfo)

  def update(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] =
    db.run(updateAction(loginInfo, authInfo)).map(_ => authInfo)

  def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    val query = for {
      result <- loginInfoQuery(loginInfo).joinLeft(slickOAuth2Infos).on(_.id === _.loginInfoId)
    } yield result
    val action = query.result.head.flatMap {
      case (dbLoginInfo, Some(dbOAuth2Info)) => updateAction(loginInfo, authInfo)
      case (dbLoginInfo, None) => addAction(loginInfo, authInfo)
    }.transactionally
    db.run(action).map(_ => authInfo)
  }

  def remove(loginInfo: LoginInfo): Future[Unit] =
    db.run(oAuth2InfoSubQuery(loginInfo).delete).map(_ => ())
}
