package com.zeimyth.models

import anorm._
import anorm.SqlParser._
import java.util.Date
import play.api.db.DB
import play.api.Play.current

/**
 * @param id An extremely insecure way to identify each client connection
 * @param created
 * @param lastComunication
 * @param userId
 */
case class Connection(
	id: Long,
	created: Date,
	lastCommunication: Date,
	userId: Option[Long]
)

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

	private def ensurePatches {
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
		try {
			Some(connectionMap(id))
		}
		catch {
			case e: NoSuchElementException => None
		}
	}
}