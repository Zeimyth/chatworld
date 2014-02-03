package com.zeimyth.controllers.api

import com.zeimyth.models.ConnectionModel

import play.api.libs.json.Json
import play.api.mvc._

import play.Logger

object ConnectionApiController extends Controller {
	val motd = "This is the message of the day."

	def connect = Action { implicit request =>
		val id = ConnectionModel.newConnection()
		Logger.trace("Received new connection from " + request.remoteAddress + " as connection " + id)

		Ok(com.zeimyth.views.api.json.MessageOfTheDay(motd)).withCookies(Cookie("connectionId", id.toString))
	}

	def disconnect = Action { implicit request =>
		val id = 0L;
		Logger.trace("Received disconnect from " + id)
		ConnectionModel.closeConnection(id)

		Ok("")
	}

	def login = Action(parse.json) { implicit request =>
		Logger.trace("Received login from " + request.remoteAddress)
		Ok("")
	}

	def logout = Action { implicit request =>
		Logger.trace("Received logout from " + request.remoteAddress)
		Ok("")
	}

	def create = Action(parse.json) { implicit request =>
		Logger.trace("Received create from " + request.remoteAddress)
		Ok("")
	}
}