package com.zeimyth.controllers.api

import com.zeimyth.models.AccountModel
import com.zeimyth.models.ConnectionModel
import com.zeimyth.models.UserInfo

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._

import play.api.Play

import play.Logger

object ConnectionApiController extends Controller {

	val userForm = Form(
		mapping(
			"name" -> nonEmptyText,
			"password" -> nonEmptyText
		)(UserInfo.apply)(UserInfo.unapply)
	)

	val motd = Play.current.configuration.getString("motd").get

	def connect = Action { implicit request =>
		val id = ConnectionModel.newConnection()
		Logger.trace("Received new connection from " + request.remoteAddress + " as connection " + id)

		Ok(com.zeimyth.views.api.json.MessageOfTheDay(motd)).withCookies(Cookie("connectionId", id.toString))
	}

	def disconnect = Action { implicit request =>
		val id = 0L
		Logger.trace("Received disconnect from " + id)
		ConnectionModel.closeConnection(id)

		Ok("")
	}

	def login = Action(parse.json) { implicit request =>
		userForm.bindFromRequest.fold(
			formWithErrors => {
				Logger.trace("Received malformed login request from " + request.remoteAddress)
				BadRequest("")
			},
			user => {
				Logger.trace("Received login from " + request.remoteAddress + ": (" + user.username + ", " + user.password + ")")

				Ok("")
			}
		)

	}

	def logout = Action { implicit request =>
		Logger.trace("Received logout from " + request.remoteAddress)
		Ok("")
	}

	def create = Action(parse.json) { implicit request =>
		// TODO: Allow user creation properly. Handle creation pipeline. Allow client to create users
		userForm.bindFromRequest.fold(
			formWithErrors => {
				Logger.trace("Received malformed create request from " + request.remoteAddress)
				BadRequest("")
			},
			user => {
				Logger.trace("Received login from " + request.remoteAddress + ": (" + user.username + ", " + user.password + ")")

				if (AccountModel.newAccount(user.username, user.password).isDefined) {
					Ok("")	
				}
				else {
					BadRequest("")
				}
			}
		)
	}
}