package com.zeimyth.views.api.json

import play.api.libs.json._

object Message {
	def apply(message: String) = {
		Default(true, Json.toJson(Map("message" -> message)))
	}

	def good(message: String) = {
		this(message)
	}

	def bad(message: String) = {
		Default(false, Json.toJson(Map("message" -> message)))
	}
}