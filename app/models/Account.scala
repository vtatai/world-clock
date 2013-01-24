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
                    active: Boolean
                    )

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
      get[Boolean]("account.active") map {
      case id ~ uuid ~ email ~ name ~ phoneNumber ~ website ~ active =>
        Account(id, uuid, Some(email), Some(name), Some(phoneNumber), Some(website), active)
    }
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
   * @param id The account id
   * @param account The account values.
   */
  def update(id: Long, account: Account) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          update account
          set uuid = {uuid}, email = {email}, name = {name}, phone_number = {phoneNumber}, website = {website}, active = {active}
          where id = {id}
          """
        ).on(
          'id -> id,
          'uuid -> account.uuid,
          'email -> account.email,
          'name -> account.name,
          'phoneNumber -> account.phoneNumber,
          'website -> account.website,
          'active -> account.active
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
            (uuid, email, name, phone_number, website, active)
          values (
            {uuid}, {email}, {name}, {phoneNumber}, {website}, {active}
          )
          """
        ).on(
          'uuid -> account.uuid,
          'name -> account.name,
          'email -> account.email,
          'phoneNumber -> account.phoneNumber,
          'website -> account.website,
          'active -> account.active
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

  /**
   * Parses an account from a company node received from AppDirect.
   *
   * @param company The node
   * @return The Account
   */
  def parseFromXml(company: Node) =
    Account(
      uuid = company \\ "uuid" text,
      email = Some(company \\ "email" text),
      name = Some(company \\ "name" text),
      phoneNumber = Some(company \\ "phoneNumber" text),
      website = Some(company \\ "website" text),
      active = true
    )
}
