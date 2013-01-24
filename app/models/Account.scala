package models

import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db.DB

/**
 * Account represents an account for a customer signing up.
 */
case class Account(
                    id: Pk[Long] = NotAssigned,
                    uuid: String,
                    email: Option[String],
                    name: Option[String],
                    phoneNumber: Option[String],
                    website: Option[String]
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
      get[String]("account.website") map {
      case id ~ uuid ~ email ~ name ~ phoneNumber ~ website =>
        Account(id, uuid, Some(email), Some(name), Some(phoneNumber), Some(website))
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
          set uuid = {uuid}, email = {email}, name = {name}, phone_number = {phoneNumber}, website = {website}
          where id = {id}
          """
        ).on(
          'id -> id,
          'uuid -> account.uuid,
          'email -> account.email,
          'name -> account.name,
          'phoneNumber -> account.phoneNumber,
          'website -> account.website
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
            (uuid, email, name, phone_number, website)
          values (
            {uuid}, {email}, {name}, {phoneNumber}, {website}
          )
          """
        ).on(
          'uuid -> account.uuid,
          'name -> account.name,
          'email -> account.email,
          'phoneNumber -> account.phoneNumber,
          'website -> account.website
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
