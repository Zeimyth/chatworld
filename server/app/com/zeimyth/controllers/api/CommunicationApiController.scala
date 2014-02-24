package com.zeimyth.controllers.api

import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

object CommunicationApiController extends Controller {

	case class input(text: String)

	val inputForm = Form(
		mapping(
			"text" -> text
		)(input.apply)(input.unapply)
	)

	def say() = Action(parse.json) { implicit request =>
		val input = inputForm.bindFromRequest.get

		Ok(com.zeimyth.views.api.json.Message("You say, \"" + input.text + "\""))
	}
}