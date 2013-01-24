package controllers

import play.api._
import libs.oauth._
import libs.oauth.ConsumerKey
import libs.oauth.OAuth
import libs.oauth.OAuthCalculator
import libs.oauth.ServiceInfo
import libs.ws.WS
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views._
import models._
import oauth.signpost.signature.OAuthMessageSigner

object Subscription extends Controller {
  val Home = Redirect(routes.Application.index)

  def create(token: String, url: String) = Action { implicit request =>
    Logger.info("Create action called " + request)
//    val authorization = request.headers("AUTHORIZATION")
//    val oauthNonce = request.headers("oauth_nonce")
//    val oauthTs = request.headers("oauth_timestamp")
//    val oauthConsumerKey = request.headers("oauth_consumer_key")
//    val oauthSignatureMethod = request.headers("oauth_signature_method")
//    val oauthVersion = request.headers("oauth_version")
//    val oauthSignature = request.headers("oauth_signature")
//    Logger.info("headers: %s | %s | %s | %s | %s | %s" format (oauthNonce, oauthTs, oauthConsumerKey, oauthSignatureMethod, oauthVersion, oauthSignature))

    val consumerKey = ConsumerKey("world-clock-4631", "wgQwLcBAXKVMO3m9")
    val oAuth = OAuth(ServiceInfo(null, null, null, consumerKey))
    val calc = OAuthCalculator(consumerKey, RequestToken("", ""))
    calc.setSendEmptyTokens(true)

    Async {
      WS.url(url).sign(calc).get().map { response =>
        Logger.info("Response: " + response)
        Ok
      }
    }
  }
  
  def update() = Action {
    Logger.info("Update action called")
    Home
  }
  
  def cancel() = Action {
    Logger.info("Cancel action called")
    Home
  }
  
  def notification() = Action {
    Logger.info("Notification action called")
    Home
  }
}