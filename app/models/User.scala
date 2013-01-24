package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import anorm.~

import play.api.Play.current
import xml.Node

/**
 * Represents a user who is purchasing the app.
 */
case class User(
                 id: Pk[Long] = NotAssigned,
                 email: String,
                 firstName: Option[String],
                 lastName: Option[String],
                 openId: Option[String],
                 language: Option[String],
                 accountId: Option[Long]
                 );

/**
 * Needs to DRY this all up together with Account.
 */
object User {

  // -- Parsers

  /**
   * Parse an User from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("user.id") ~
      get[String]("user.email") ~
      get[String]("user.first_name") ~
      get[String]("user.last_name") ~
      get[String]("user.open_id") ~
      get[String]("user.language") ~
      get[Long]("user.account_id") map {
      case id ~ email ~ firstName ~ lastName ~ openId ~ language ~ accountId =>
        User(id, email, Some(firstName), Some(lastName), Some(openId), Some(language), Some(accountId))
    }
  }

  // -- Queries

  /**
   * Retrieve an User by id.
   */
  def findById(id: Long): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from user where id = {id}").on('id -> id).as(User.simple.singleOpt)
    }
  }

  /**
   * Retrieve an User by id.
   */
  def findByEmail(email: String): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from \"user\" where email = {email}").on('email -> email).as(User.simple.singleOpt)
    }
  }

  /**
   * Update an User.
   *
   * @param id The user id
   * @param user The user values.
   */
  def update(id: Long, user: User) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          update user
          set email = {email}, first_name = {firstName}, last_name = {lastName}, open_id = {openId}, language = {language}, account_id = {accountId}
          where id = {id}
          """
        ).on(
          'id -> id,
          'email -> user.email,
          'firstName -> user.firstName,
          'lastName -> user.lastName,
          'openId -> user.openId,
          'language -> user.language,
          'accountId -> user.accountId
        ).executeUpdate()
    }
  }

  /**
   * Insert a new user.
   *
   * @param user The user values.
   */
  def insert(user: User) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          insert into "user"
            (email, first_name, last_name, open_id, language, account_id)
          values (
            {email}, {firstName}, {lastName}, {openId}, {language}, {accountId}
            )
          """
        ).on(
          'email -> user.email,
          'firstName -> user.firstName,
          'lastName -> user.lastName,
          'openId -> user.openId,
          'language -> user.language,
          'accountId -> user.accountId
        ).executeInsert()
    }
  }

  /**
   * Delete a user.
   *
   * @param id Id of the user to delete.
   */
  def delete(id: Long) = {
    DB.withConnection {
      implicit connection =>
        SQL("delete from user where id = {id}").on('id -> id).executeUpdate()
    }
  }

  /**
   * Parses an user from a creator node received from AppDirect
   *
   * @param creator The node
   * @param accountId The account id
   * @return The user
   */
  def parseFromXml(creator: Node, accountId: Option[Long]) =
    User(
      email = creator \\ "email" text,
      firstName = Some(creator \\ "firstName" text),
      lastName = Some(creator \\ "lastName" text),
      openId = Some(creator \\ "openId" text),
      language = Some(creator \\ "language" text),
      accountId = accountId)
}
