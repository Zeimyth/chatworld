package com.zeimyth.controllers.api

import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.mvc.{Action, Controller}

object InputController extends Controller {

	case class input(text: String)

	val inputForm = Form(
		mapping(
			"text" -> text
		)(input.apply)(input.unapply)
	)

	def handleInput = Action { implicit request =>
		Ok(com.zeimyth.views.api.json.Message("I hear you."))
	}

	def echo = Action(parse.json) { implicit request =>
		val input = inputForm.bindFromRequest.get

		Ok(com.zeimyth.views.api.json.Message("There is no command for: " + input.text))
	}
}