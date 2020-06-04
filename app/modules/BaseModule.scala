package modules

import com.google.inject.{AbstractModule, Provides}
import javax.inject.Named
import models.auth.daos.{LoginInfoDAO, LoginInfoDAOImpl}
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.concurrent.AkkaGuiceSupport


class BaseModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bind[LoginInfoDAO].to[LoginInfoDAOImpl]
  }

  @Named("SendGridApiKey")
  @Provides
  def providesSendGridApiKey(conf: Configuration): String = {
    conf.get[String]("sendgrid.api.key")
  }
}