package models

import anorm._
import anorm.SqlParser._

case class Company(id: Pk[Long] = NotAssigned, name: String)