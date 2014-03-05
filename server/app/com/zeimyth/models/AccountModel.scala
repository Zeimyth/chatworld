package com.zeimyth.models

import com.zeimyth.utils.ListenManager
import com.zeimyth.utils.MessageType._

import java.util.Date

import play.Logger

case class Account(id: Long,
                   username: String,
                   password: String,
                   created: Date,
                   lastLoggedIn: Date,
                   connectionId: Option[Long]) {

	def copy(id: Long = id,
	         username: String = username,
	         password: String = password,
	         created: Date = created,
	         lastLoggedIn: Date = lastLoggedIn,
	         connectionId: Option[Long] = connectionId) = {

		Account(id, username, password, created, lastLoggedIn, connectionId)
	}
}

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

	def getAccount(id: Long): Option[Account] = {
		accountMap.get(id)
	}

	def getAccountByUsername(username: String): Option[Account] = {
		val lowercaseUsername = username.toLowerCase
		accountMap.values.find(_.username.toLowerCase == lowercaseUsername)
	}

	private def isPasswordValid(password: String) = {
		// Passwords should be comprised of letters, numbers, or underscores only
		password.matches("^\\w+$")
	}

	private def createNewAccount(username: String, password: String) = {
		val id = nextId()
		val now = new Date()

		val account = Account(id, username, password, now, now, None)
		accountMap += (id -> account)

		Logger.trace("New account created with id " + id + " and name " + username)
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
					login(account, connection)
				}
				else {
					false
				}
			case None => false
		}
	}

	def login(account: Account, connection: Connection): Boolean = {
		if (account.connectionId.isDefined) {
			// The user was already logged in through another connection; deal with that here
		}

		accountMap += (account.id -> account.copy(connectionId = Some(connection.id)))
		ConnectionModel.addUserIdToConnection(connection.id, account.id)

		ListenManager.addMessage(account.username + " wakes up.", account.id, Login)

		true
	}

	def logout(userId: Long) {
		getAccount(userId) match {
			case Some(account) => accountMap -= account.id
			case None =>
		}
	}
}
