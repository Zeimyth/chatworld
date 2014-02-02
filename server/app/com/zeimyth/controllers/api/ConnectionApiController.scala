package com.zeimyth.controllers.api

import com.zeimyth.models.ConnectionModel
import play.api.libs.json.Json
import play.api.mvc._

object ConnectionApiController extends Controller {
	val motd = "This is the message of the day."

	def connect = Action { implicit request =>
		val id = ConnectionModel.newConnection()

		Ok(com.zeimyth.views.api.json.MessageOfTheDay(motd)).withCookies(Cookie("connectionId", id.toString))
	}

	def disconnect = Action { implicit request =>
		Ok()
	}

	def login = Action(parse.json) { implicit request =>

	}

	def logout = Action(parse.json) { implicit request =>

	}

	def create = Action(parse.json) { implicit request =>

	}
}