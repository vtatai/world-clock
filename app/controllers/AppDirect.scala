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

/**
 * This is a common base trait for controllers handling AppDirect requests.
 */
trait AppDirect extends Controller {

  /**
   * The error code, following AppDirect's API.
   */
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

  /**
   * Uses Play's OAuth support to create an OAuthCalculator which is used to sign all requests to AppDirect.
   * TODO keys are hardcoded now, move them to config file
   *
   * @return The OAuthCalculator
   */
  def createOAuthCalculator = {
    val consumerKey = ConsumerKey("world-clock-4631", "wgQwLcBAXKVMO3m9")
    val calc = OAuthCalculator(consumerKey, RequestToken("", ""))
    //    calc.setSendEmptyTokens(true)
    calc
  }

  /**
   * Sends a successful response.
   *
   * @param message The message to send
   * @return The SimpleResult (always Ok)
   */
  def sendResponse(message: String) = {
    val response = <result>
      <success>true</success>
      <message>{message}</message>
    </result>
    Logger.debug("Sending response" + response)
    Ok(response)
  }

  /**
   * Sends an error response.
   *
   * @param message The message to send
   * @param errorCode The error code according to AppDirect's API
   * @return The SimpleResult (always Ok)
   */
  def sendErrorResponse(message: String, errorCode: String) = {
    val response = <result>
      <success>false</success>
      <message>{message}</message>
      <errorCode>{errorCode}</errorCode>
    </result>
    Logger.debug("Sending response" + response)
    Ok(response)
  }

  /**
   * This method is reused by all the other methods, except for the create method which has a different logic.
   *
   * @param f A function to execute if the account was found and is valid
   * @return The action
   */
  def action(f: (Account, play.api.libs.ws.Response) => SimpleResult[_]) = Action { implicit request =>
  // TODO Need to authorize the request using oauth!
    Logger.info("Action called " + request)
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
            case Some(x) => f(x, response)
            case None => sendErrorResponse("Account does not exist", ErrorCode.AccountNotFound.toString)
          }
        }
      }
    }
  }
}
