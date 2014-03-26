package com.zeimyth.models

import anorm._
import anorm.SqlParser._
import java.util.Date
import play.api.db.DB
import play.api.Play.current

/**
 * @param id An extremely insecure way to identify each client connection
 * @param created
 * @param lastCommunication
 * @param userId
 */
case class Connection(
	id: Long,
	created: Date,
	lastCommunication: Date,
	userId: Option[Long]) {

	def copy(id: Long = id,
			created: Date = created,
			lastCommunication: Date = lastCommunication, 
			userId: Option[Long] = userId) = {

		Connection(id, created, lastCommunication, userId)
	}
}

object ConnectionModel {

	// private val connectionParser: RowParser[Connection] = {
	// 	get[Long]("id") ~
	// 	get[Date]("created") ~
	// 	get[Date]("last_communication") ~
	// 	get[Option[Long]]("user_id") map {
	// 		case id ~ created ~ last_communication ~ user_id =>
	// 				Connection(id, created, last_communication, user_id)
	// 	}
	// }

	// def newConnection() = {
	// 	ensurePatches

	// 	val id = getNextId()

	// 	DB.withConnection { implicit connection =>
	// 		SQL("INSERT INTO `connections`(id, created, last_communication) VALUES({id}, NOW(), NOW())")
	// 			.on("id" -> id)
	// 			.executeInsert()
	// 	}

	// 	id
	// }

	// def getConnection(id: Long): Connection = {
	// 	ensurePatches

	// 	DB.withConnection { implicit connection =>
	// 		SQL("SELECT `id, `created, `last_communication, `user_id FROM `connections WHERE `id={id}")
	// 			.on("id" -> id)
	// 			.as(connectionParser single)
	// 	}
	// }

	private val connectionMap = scala.collection.mutable.Map[Long, Connection]()

	private def ensurePatches() {
		PatchManager.ensurePatches()
	}
	
	private var nextConnectionId = -1L

	private def getNextId() = {
		nextConnectionId = nextConnectionId + 1
		nextConnectionId
	}

	def newConnection() = {
		val id = getNextId()
		val now = new Date()

		connectionMap += (id -> Connection(id, now, now, None))

		id
	}

	def getConnection(id: Long): Option[Connection] = {
		connectionMap.get(id)
	}

	def getAllActiveConnections: Seq[Connection] = {
		connectionMap.map(_._2).toSeq
	}

	def getUserIdByConnection(id: Long): Option[Long] = {
		getConnection(id) match {
			case Some(connection) => connection.userId
			case None => None
		}
	}

	def addUserIdToConnection(id: Long, userId: Long) {
		getConnection(id) match {
			case Some(connection) => connection.userId match {
				case Some(currentUserId) => if (currentUserId != userId) {
					throw new IllegalStateException("Connection " + id +
							" is already associated with userId " + currentUserId)
				}
				case None => connectionMap += (id -> connection.copy(userId = Some(userId)))
			}
			case None => throw new NoSuchElementException(
					"Connection " + id + " does not exist in the database")
		}
	}

	def closeConnection(id: Long) {
		getConnection(id) match {
			case Some(connection) =>
				connectionMap -= id
				connection.userId match {
					case Some(userId) => AccountModel.logout(userId)
					case None =>
				}
			case None =>
		}
	}
}