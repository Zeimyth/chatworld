package com.zeimyth.models.migrations

import java.sql.{Connection, Statement}

trait Patch {
	def execute(conn: Connection): Unit
	def unexecute(conn: Connection): Unit

	protected def withStatement[A](conn: Connection)(f: (Statement) => A): A = {
		var statement: Statement = null
		try{
			statement = conn.createStatement()
			f(statement)
		}
		finally {
			if (statement != null) {
				statement.close()
			}
		}
	}
}