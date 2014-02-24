package com.zeimyth.models.migrations

import java.sql.Connection

class Patch001 extends Patch {
	override def execute(conn: Connection) {
		withStatement(conn) { statement =>
			statement.execute("""
				CREATE TABLE `connections` (
					`id` BIGINT NOT NULL,
					`created` datetime NOT NULL,
					`last_communication` datetime NOT NULL,
					`user_id` BIGINT,
					PRIMARY KEY (`id`)
				)
				""")
		}
	}

	override def unexecute(conn: Connection) {
		withStatement(conn) { statement =>
			statement.execute("""
				DROP TABLE `connections`
				""")
		}
	}
}