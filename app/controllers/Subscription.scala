package controllers

import play.api._
import libs.ws.WS
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._


import models._

/**
 * Receives AppDirect subscription requests.
 */
object Subscription extends AppDirect {
  val Home = Redirect(routes.Application.index)

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
        val parsedAccount: Account = Account.parseFromXml(response.xml)
        Account.findByUuid(parsedAccount.uuid) match {
          case Some(x) => sendCreateResponse("Account already exists, returning old id", x.id.toString)
          case None => {
            val accountId = Account.insert(parsedAccount)
            val user = User.parseFromXml(response.xml \\ "event" \ "creator" head, accountId)
            User.findByEmail(user.email) match {
              case Some(x) => sendErrorResponse("User already exists", ErrorCode.UserAlreadyExists.toString)
              case None => {
                User.insert(user)
                sendCreateResponse("Account created successfully", accountId.get.toString)
              }
            }
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

  /**
   * Responds to an update request.
   */
  def update() = {
    def updateAction(account: Account, response: play.api.libs.ws.Response) = {
      Account.updatePlan(account.id.get, (response.xml \\ "order" \ "editionCode").text)
      sendResponse("Update ok")
    }
    action(updateAction)
  }

  /**
   * Responds to a cancel request.
   */
  def cancel() = {
    def cancelAction(account: Account, response: play.api.libs.ws.Response) = {
      Account.delete(account.id.get)
      sendResponse("Account canceled")
    }
    action(cancelAction)
  }

  /**
   * Responds to a notification request.
   */
  def notification() = {
    def notificationAction(account: Account, response: play.api.libs.ws.Response) = {
      (response.xml \\ "payload" \ "account" \ "status").text match {
        case "DEACTIVATED" => {
          Account.deactivate(account.id.get)
          sendResponse("Account deactivated")
        }
        case "REACTIVATED" => {
          Account.activate(account.id.get)
          sendResponse("Account reactivated")
        }
        case "UPCOMING_INVOICE" => sendResponse("NOT SUPPORTED YET")
        case _ => sendErrorResponse("Unknown notification code", ErrorCode.InvalidResponse.toString)
      }
    }
    action(notificationAction)
  }
}