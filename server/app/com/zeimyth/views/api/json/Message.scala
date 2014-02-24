package com.zeimyth.views.api.json

import play.api.libs.json._

object Message {
	def apply(message: String) = {
		Default(true, Json.toJson(Map("message" -> message)))
	}
}