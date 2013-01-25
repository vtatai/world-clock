package controllers


import models._

/**
 * This receives AppDirect user events.
 */
object UserAssign extends AppDirect {
  /**
   * Responds to an user assignment request.
   */
  def assign = {
    def assignAction(account: Account, response: play.api.libs.ws.Response) = {
      val user = User.parseFromXml(response.xml \\ "payload" \ "user" head, Some(account.id.get))
      User.findByEmail(user.email) match {
        case Some(u) => sendErrorResponse("User already exists", ErrorCode.UserAlreadyExists.toString)
        case None => {
          User.insert(user)
          sendResponse("User created and assigned")
        }
      }
    }
    action(assignAction)
  }

  /**
   * Responds to an user unassignment request.
   */
  def unassign = {
    def unassignAction(account: Account, response: play.api.libs.ws.Response) = {
      val parsedUser = User.parseFromXml(response.xml \\ "payload" \ "user" head, Some(account.id.get))
      User.findByEmail(parsedUser.email) match {
        case None => sendErrorResponse("User does not exist", ErrorCode.UserNotFound.toString)
        case Some(user) => {
          User.delete(user.id.get)
          sendResponse("User unassigned")
        }
      }
    }
    action(unassignAction)
  }
}
