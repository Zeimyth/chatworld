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

	def info: String = {
		username + " #" + id
	}
}

case class UserInfo(username: String, password: String)

object AccountModel {
	private val accountMap = scala.collection.mutable.Map[Long, Account]()

	private var nextAccountId = -1L

	def newAccount(username: String, password: String): Either[Account, String] = {
		if (isUsernameUnique(username)) {
			if (isPasswordValid(password)) {
				Left(createNewAccount(username, password))
			}
			else {
				Right("invalid password.")
			}
		}
		else {
			Right("that username is already taken.")
		}
	}

	private def isUsernameUnique(username: String) = {
		getAccountByUsername(username).isEmpty
	}

	def getAccount(id: Long): Option[Account] = {
		accountMap.get(id)
	}

	def getAccountByConnectionId(connectionId: Long): Option[Account] = {
		ConnectionModel.getUserIdByConnection(connectionId) match {
			case Some(userId) => getAccount(userId)
			case None => None
		}
	}

	def getAccountByUsername(username: String): Option[Account] = {
		val lowercaseUsername = username.toLowerCase
		accountMap.values.find(_.username.toLowerCase == lowercaseUsername)
	}

	def whois(name: String): Option[Account] = {
		getAccountByUsername(name)
	}

	def whoisOnline(): Seq[Account] = {
		ConnectionModel.getAllActiveConnections
			.map { connection =>
				getAccountByConnectionId(connection.id)
			}
			.filter(_.isDefined)
			.map(_.get)
			.toSeq
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
			Logger.warn(account.info + " logging in with connection " + connection.id + " when already logged in at " +
				"connection " + account.connectionId + "")
		}

		accountMap += (account.id -> account.copy(connectionId = Some(connection.id), lastLoggedIn = new Date()))
		ConnectionModel.addUserIdToConnection(connection.id, account.id)
		ListenManager.addMessage(account.username + " wakes up.", account.id, Login)

		Logger.debug("Connection " + connection.id + " logged in as " + account.info)

		true
	}

	def logout(userId: Long) {
		getAccount(userId) match {
			case Some(account) =>
				account.connectionId match {
					case Some(connectionId) => ConnectionModel.removeUserIdFromConnection(connectionId)
					case None => Logger.warn("Logging out user " + account.info + " with no connectionId")
				}
				accountMap += (account.id -> account.copy(connectionId = None))

				ListenManager.addMessage(account.username + " falls asleep.", account.id, Logout)
				Logger.debug(account.info + " logged out")

			case None => Logger.warn("Logout attempted for nonexistent user " + userId)
		}
	}
}
