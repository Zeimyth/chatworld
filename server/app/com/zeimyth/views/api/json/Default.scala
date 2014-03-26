package com.zeimyth.views.api.json

import play.api.libs.json._

object Default {
	def apply(success: Boolean, content: JsValue): JsValue = {
		Json.toJson(Map("status" -> Json.toJson(if (success) "success" else "failure"),
								"content" -> content))
	}
}