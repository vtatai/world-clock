package models

import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db.DB
import xml.Node

/**
 * Account represents an account for a customer signing up.
 */
case class Account(
                    id: Pk[Long] = NotAssigned,
                    uuid: String,
                    email: Option[String],
                    name: Option[String],
                    phoneNumber: Option[String],
                    website: Option[String],
                    active: Boolean,
                    plan: String
                    )

/**
 * Object responsible for parsing and CRUD operations for Account.
 * TODO DRY this up together with User
 */
object Account {

  // -- Parsers

  /**
   * Parse an Account from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("account.id") ~
      get[String]("account.uuid") ~
      get[String]("account.email") ~
      get[String]("account.name") ~
      get[String]("account.phone_number") ~
      get[String]("account.website") ~
      get[Boolean]("account.active") ~
      get[String]("account.plan") map {
      case id ~ uuid ~ email ~ name ~ phoneNumber ~ website ~ active ~ plan =>
        Account(id, uuid, Some(email), Some(name), Some(phoneNumber), Some(website), active, plan)
    }
  }


  /**
   * Parses an account from a company node received from AppDirect.
   *
   * @param event The event node
   * @return The Account
   */
  def parseFromXml(event: Node) = {
    val company = (event \\ "company").head
    Account(
      uuid = company \\ "uuid" text,
      email = Some(company \\ "email" text),
      name = Some(company \\ "name" text),
      phoneNumber = Some(company \\ "phoneNumber" text),
      website = Some(company \\ "website" text),
      active = true,
      plan = event \\ "order" \ "editionCode" text
    )
  }

  // -- Queries

  /**
   * Retrieve a account from the id.
   */
  def findById(id: Long): Option[Account] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from account where id = {id}").on('id -> id).as(Account.simple.singleOpt)
    }
  }

  /**
   * Retrieve a account from the uuid.
   */
  def findByUuid(uuid: String): Option[Account] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from account where uuid = {uuid}").on('uuid -> uuid).as(Account.simple.singleOpt)
    }
  }

  /**
   * Update a account.
   *
   * @param account The account values.
   */
  def update(account: Account) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          update account
          set uuid = {uuid}, email = {email}, name = {name}, phone_number = {phoneNumber}, website = {website}, active = {active}, plan = {plan}
          where id = {id}
          """
        ).on(
          'id -> account.id.get,
          'uuid -> account.uuid,
          'email -> account.email,
          'name -> account.name,
          'phoneNumber -> account.phoneNumber,
          'website -> account.website,
          'active -> account.active,
          'plan -> account.plan
        ).executeUpdate()
    }
  }

  /**
   * Sets the active flag for an account.
   *
   * @param id The account id
   * @param active Value to set
   */
  def updateActive(id: Long, active: Boolean) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          update account
          set active = {active}
          where id = {id}
          """
        ).on(
          'id -> id,
          'active -> active
        ).executeUpdate()
    }
  }

  /**
   * Deactivates an account.
   */
  def deactivate(id: Long) = updateActive(id, false)

  /**
   * Activates an account.
   */
  def activate(id: Long) = updateActive(id, true)

  /**
   * Updates the plan.
   *
   * @param id The account id
   * @param plan The plan
   */
  def updatePlan(id: Long, plan: String) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          update account set plan = {plan} where id = {id}
          """
        ).on(
          'id -> id,
          'plan -> plan
        ).executeUpdate()
    }
  }

  /**
   * Insert a new account.
   *
   * @param account The account values.
   */
  def insert(account: Account) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          insert into account
            (uuid, email, name, phone_number, website, active, plan)
          values (
            {uuid}, {email}, {name}, {phoneNumber}, {website}, {active}, {plan}
          )
          """
        ).on(
          'uuid -> account.uuid,
          'name -> account.name,
          'email -> account.email,
          'phoneNumber -> account.phoneNumber,
          'website -> account.website,
          'active -> account.active,
          'plan -> account.plan
        ).executeInsert()
    }
  }

  /**
   * Delete a account.
   *
   * @param id Id of the account to delete.
   */
  def delete(id: Long) = {
    DB.withConnection {
      implicit connection =>
        SQL("delete from account where id = {id}").on('id -> id).executeUpdate()
    }
  }
}