package controllers

import play.api._
import libs.oauth._
import libs.oauth.ConsumerKey
import libs.oauth.OAuthCalculator
import libs.ws.WS
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._


import models._
import scala.xml.Node

/**
 * Receives AppDirect subscription requests.
 */
object Subscription extends Controller {
  val Home = Redirect(routes.Application.index)

  object ErrorCode extends Enumeration {
    type ErrorCode = Value

    val UserAlreadyExists = Value("USER_ALREADY_EXISTS")
    val UserNotFound = Value("USER_NOT_FOUND")
    val AccountNotFound = Value("ACCOUNT_NOT_FOUND")
    val MaxUsersReached = Value("MAX_USERS_REACHED")
    val Unauthorized = Value("UNAUTHORIZED")
    val OperationCanceled = Value("OPERATION_CANCELED")
    val ConfigurationError = Value("CONFIGURATION_ERROR")
    val InvalidResponse = Value("INVALID_RESPONSE")
    val UnknownError = Value("UNKNOWN_ERROR")
  }

  def createOAuthCalculator = {
    val consumerKey = ConsumerKey("world-clock-4631", "wgQwLcBAXKVMO3m9")
    val calc = OAuthCalculator(consumerKey, RequestToken("", ""))
    //    calc.setSendEmptyTokens(true)
    calc
  }

  /**
   * Responds to a create request.
   *
   * As an observation, since I could not find in the docs, if an account with the same UUID is found, or if an user
   * with the same email is found, then an error message is returned
   *
   * @return The action to be executed
   */
  def create = Action { implicit request =>
    // TODO Need to authorize the request using oauth!
    Logger.info("Create action called " + request)

    val form = Form(tuple("token" -> text, "eventUrl" -> text))
    val (token, eventUrl) = form.bindFromRequest().get
    Logger.info("Token %s url eventUrl %s".format(token, eventUrl))
    Async {
      WS.url(eventUrl).sign(createOAuthCalculator).get().map { response =>
        Logger.debug("Response to callback: %s" format response.xml)
        val parsedAccount: Account = parseAccount(response.xml \\ "payload" \ "company" head)
        Account.findByUuid(parsedAccount.uuid) match {
          case Some(x) => sendCreateResponse("Account already exists, returning old id", x.id.toString)
          case None => {
            val accountId = Account.insert(parsedAccount)
            sendCreateResponse("Account created successfully", accountId.get.toString)
          }
        }
      }
    }
  }

  def sendCreateResponse(message: String, accountIdentifier: String) = {
    val response = <result>
      <success>true</success>
      <message>{message}</message>
      <accountIdentifier>{accountIdentifier}</accountIdentifier>
    </result>
    Logger.debug("Sending response" + response)
    Ok(response)
  }

  def sendResponse(message: String) = {
    val response = <result>
      <success>true</success>
      <message>{message}</message>
    </result>
    Logger.debug("Sending response" + response)
    Ok(response)
  }

  def sendErrorResponse(message: String, errorCode: String) = {
    val response = <result>
      <success>false</success>
      <message>{message}</message>
      <errorCode>{errorCode}</errorCode>
    </result>
    Logger.debug("Sending response" + response)
    Ok(response)
  }

  def parseUser(creator: Node, accountId: Option[Long]) =
    User(
      email = creator \\ "email" text,
      firstName = Some(creator \\ "firstName" text),
      lastName = Some(creator \\ "lastName" text),
      openId = Some(creator \\ "openId" text),
      language = Some(creator \\ "language" text),
      accountId = accountId)

  def parseAccount(company: Node) =
    Account(
      uuid = company \\ "uuid" text,
      email = Some(company \\ "email" text),
      name = Some(company \\ "name" text),
      phoneNumber = Some(company \\ "phoneNumber" text),
      website = Some(company \\ "website" text)
    )

  def update() = Action { implicit request =>
    // TODO Need to authorize the request using oauth!
    Logger.info("Update action called " + request)

    val eventUrl = Form("eventUrl" -> text).bindFromRequest().get
    Async {
      WS.url(eventUrl).sign(createOAuthCalculator).get().map { response =>
        Logger.debug("Response to callback: %s" format response.xml)
        val accountIdentifier: String = (response.xml \\ "payload" \ "account" \ "accountIdentifier").text
        if (accountIdentifier == "dummy-account") {
          sendResponse("dummy-account, sending ok reply")
        } else if (!accountIdentifier.matches("\\d+")) {
          sendErrorResponse("Account does not exist", ErrorCode.AccountNotFound.toString)
        } else {
          Account.findById(accountIdentifier.toLong) match {
            case Some(x) => sendResponse("Update ok") // Probably do something else here in a real app
            case None => sendErrorResponse("Account does not exist", ErrorCode.AccountNotFound.toString)
          }
        }
      }
    }
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