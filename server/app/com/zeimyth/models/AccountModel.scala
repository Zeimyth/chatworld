package com.zeimyth.models

import java.util.Date

case class Account(id: Long,
                   username: String,
                   password: String,
                   created: Date,
                   var lastLoggedIn: Date,
                   var connectionId: Option[Long])

case class UserInfo(username: String, password: String)

object AccountModel {
	private val accountMap = scala.collection.mutable.Map[Long, Account]()

	private var nextAccountId = -1L

  def newAccount(username: String, password: String): Option[Account] = {
    if (isUsernameUnique(username) && isPasswordValid(password)) {
      Some(createNewAccount(username, password))
    }
    else {
      None
    }
  }

  private def isUsernameUnique(username: String) = {
    getAccountByUsername(username).isEmpty
  }

  def getAccountByUsername(username: String): Option[Account] = {
    accountMap.values.find(_.username == username)
  }

  private def isPasswordValid(password: String) = {
    // Passwords should be comprised of letters, numbers, or underscores only
    password.matches("^/w*$")
  }

  private def createNewAccount(username: String, password: String) = {
    val id = nextId()
    val now = new Date()

    val account = Account(id, username, password, now, now, None)
    accountMap += (id -> account)

    account
  }

	private def nextId() = {
		nextAccountId = nextAccountId + 1
		nextAccountId
	}
  
  def tryLogin(userInfo: UserInfo, connection: Connection): Boolean = {
    getAccountByUsername(userInfo.username) match {
      case Some(account) =>
        if (account.password == userInfo.password) {
          if (account.connectionId.isDefined) {
            // The user was already logged in through another connection; deal with that here
          }
          account.connectionId = Some(connection.id)
          true
        }
        else {
          false
        }
      case None => false
    }
  }
}
