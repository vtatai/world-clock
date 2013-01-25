package controllers

import play.api._
import play.api.mvc._
import java.text.SimpleDateFormat
import java.util.Date

object Application extends Controller {
  
  def index = Action {
    val sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm")
    Ok(views.html.index("Current time is %s" format sdf.format(new Date())))
  }
  
}