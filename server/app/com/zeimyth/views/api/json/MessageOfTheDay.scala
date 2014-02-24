package com.zeimyth.views.api.json

import play.api.libs.json._

object MessageOfTheDay {
	def apply(motd: String) = {
		Default(motd != "", Json.toJson(Map("motd" -> motd)))
	}
}