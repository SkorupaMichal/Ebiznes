package utils.auth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.User
class DefaultEnv extends  Env{
  type I = User
  type A = JWTAuthenticator
}


