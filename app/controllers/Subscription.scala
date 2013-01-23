package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views._
import models._

object Subscription extends Controller {
  val Home = Redirect(routes.Application.index)

  def create() = Action { implicit request =>
    Logger.info("Create action called " + request)
    val oauthNonce = request.headers("oauth_nonce")
    val oauthTs = request.headers("oauth_timestamp")
    val oauthConsumerKey = request.headers("oauth_consumer_key")
    val oauthSignatureMethod = request.headers("oauth_signature_method")
    val oauthVersion = request.headers("oauth_version")
    val oauthSignature = request.headers("oauth_signature")
    Logger.info("headers: %s | %s | %s | %s | %s | %s" format (oauthNonce, oauthTs, oauthConsumerKey, oauthSignatureMethod, oauthVersion, oauthSignature))
    Ok
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