package com.zeimyth.models

import anorm._
import java.util.Date
import play.api.db.DB
import play.api.Play.current

case class Connection(
	id: Long,
	created: Date,
	lastCommunication: Date,
	userId: Option[Long]
)

object ConnectionModel {

	private var nextConnectionId = -1L

	private def ensurePatches {
		PatchManager.ensurePatches()
	}

	private def getNextId() = {
		nextConnectionId = nextConnectionId + 1
		nextConnectionId
	}

	def newConnection() = {
		ensurePatches

		val id = getNextId()

		DB.withConnection { implicit c =>
			SQL("INSERT INTO `connections`(id, created, last_communication) VALUES({id}, NOW(), NOW())")
				.on("id" -> id)
				.executeInsert()
		}

		id
	}
}