package com.zeimyth

object Logger {
	val TRACE = 0
	val DEBUG = 1
	val INFO = 2
	val WARN = 3
	val ERROR = 4
}

class Logger[A](classVal: Class[A], val level: Int = Logger.DEBUG) {
	private val className = classVal.getName()

	def trace(message: String) {
		display(Logger.TRACE, message)
	}

	def debug(message: String) {
		display(Logger.DEBUG, message)
	}

	def info(message: String) {
		display(Logger.INFO, message)
	}

	def warn(message: String) {
		display(Logger.WARN, message)
	}

	def error(message: String) {
		display(Logger.ERROR, message)
	}

	private def display(messageLevel: Int, message: String) {
		if (messageLevel >= level) {
			println(message)
		}
	}
}