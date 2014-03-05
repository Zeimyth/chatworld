package com.zeimyth.views.api.json

import play.api.libs.json.JsNull

object Empty {
	def apply() = {
		Default(true, JsNull)
	}
}
