package com.zeimyth.views.api.json.who

import com.github.nscala_time.time.Imports._

import com.zeimyth.models.{ConnectionModel, Account}
import com.zeimyth.views.api.json.Message

import java.util.Date
import org.joda.time.{Minutes, Hours, Days}

object WhoDisplay {
	def apply(accounts: Seq[Account]) = {
		if (!accounts.isEmpty) {
			Message(new WhoDisplay(accounts).generateOutput())
		}
		else {
			Message("No users are online")
		}
	}
}

private case class ColumnWidths(nameColumn: Int, idleTimeColumn: Int, connectionTimeColumn: Int)
private case class RowContents(name: String, idleTime: String, connectionTime: String)

class WhoDisplay(accounts: Seq[Account]) {
	var NAME = "Name"
	var IDLE_FOR = "Idle For"
	var ONLINE_FOR = "Online For"

	private val rows = accounts.map(getRowContents)
	private val widths = calculateColumnWidths()
	private val builder = new StringBuilder

	def generateOutput(): String = {
		generateHeaders()
		generateRows()

		builder.toString()
	}

	private def calculateColumnWidths() = {
		val nameWidth = rows
			.map(_.name)
			.foldLeft(NAME)(longestString)
			.length
		val idleTimeWidth = rows
			.map(_.idleTime)
			.foldLeft(IDLE_FOR)(longestString)
			.length
		val connectionTimeWidth = rows
			.map(_.connectionTime)
			.foldLeft(ONLINE_FOR)(longestString)
			.length

		ColumnWidths(nameWidth, idleTimeWidth, connectionTimeWidth)
	}

	private def longestString(a: String, b: String) = {
		if (a == null) {
			b
		}
		else if (b == null) {
			a
		}
		else {
			if (a.length > b.length) {
				a
			}
			else {
				b
			}
		}
	}

	private def formatIdleTime(account:Account) = {
		account.connectionId match {
			case Some(connectionId) =>
				ConnectionModel.getConnection(connectionId) match {
					case Some(connection) =>
						formatTime(connection.lastCommunication)
					case None => "Not logged in"
				}
			case None => "Not logged in"
		}
	}

	private def formatConnectionTime(account: Account) = {
		formatTime(account.lastLoggedIn)
	}

	/**
	 * Returns a human-readable representation of the time passed since a given Date.
	 * The representation is formatted to only show the two most significant units of
	 * time; that is, 1 day 1 hour 50 minutes will only be 1 day 1 hour.
	 *
	 * @param time the time to format
	 * @return a human-readable representation of the given time
	 */
	private def formatTime(time: Date) = {
		var duration = new Duration(new DateTime(time), DateTime.now)
		val days = duration.getStandardDays
		duration = duration.minus(Days.days(days.toInt).toStandardDuration)
		val hours = duration.getStandardHours
		duration = duration.minus(Hours.hours(hours.toInt).toStandardDuration)
		val minutes = duration.getStandardMinutes
		duration = duration.minus(Minutes.minutes(minutes.toInt).toStandardDuration)
		val seconds = duration.getStandardSeconds

		days match {
			case 0L =>
				hours match {
					case 0L =>
						minutes match {
							case 0L => s"$seconds sec"
							case _ =>
								seconds match {
									case 0L => s"$minutes min"
									case _ => s"$minutes min $seconds sec"
								}
						}
					case _ =>
						minutes match {
							case 0L => s"$hours hr"
							case _ => s"$hours hr $minutes min"
						}
				}
			case _ =>
				hours match {
					case 0L => s"$days d"
					case _ => s"$days d $hours hr"
				}
		}
	}

	private def generateHeaders() {
		val headers = new RowContents(NAME, IDLE_FOR, ONLINE_FOR)

		appendRowContents(headers)
		appendColumnUnderlines()
	}

	private def generateRows() {
		rows.foreach(appendRowContents)
	}

	private def getRowContents(account: Account) = {
		RowContents(account.username, formatIdleTime(account), formatConnectionTime(account))
	}

	private def appendRowContents(row: RowContents) {
		appendColumnHeader(row.name, widths.nameColumn, 'first_column)
		appendColumnHeader(row.idleTime, widths.idleTimeColumn, 'middle_column)
		appendColumnHeader(row.connectionTime, widths.connectionTimeColumn, 'end_column)
	}

	private def appendColumnHeader(value: String, width: Int, columnType: Symbol) {
		val totalWidth = if (columnType != 'end_column) {
			width + 1
		}
		else {
			width
		}

		if (columnType != 'first_column) {
			builder.append(" ")
		}
		builder.append(value)
		for (i <- value.length until totalWidth) {
			builder.append(" ")
		}

		if (columnType != 'end_column) {
			builder.append("|")
		}
		else {
			builder.append("\n")
		}
	}

	private def appendColumnUnderlines() {
		appendColumnUnderline(widths.nameColumn, 'first_column)
		appendColumnUnderline(widths.idleTimeColumn, 'middle_column)
		appendColumnUnderline(widths.connectionTimeColumn, 'end_column)
	}

	private def appendColumnUnderline(width: Int, columnType: Symbol) {
		val totalWidth = if (columnType != 'middle_column) {
			width + 1
		}
		else {
			width + 2
		}

		for (i <- 0 until totalWidth) {
			builder.append("-")
		}

		if (columnType != 'end_column) {
			builder.append("+")
		}
		else {
			builder.append("\n")
		}
	}
}
