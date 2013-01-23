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

  def create() = Action {
    Logger.info("Create action called")
    Home
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