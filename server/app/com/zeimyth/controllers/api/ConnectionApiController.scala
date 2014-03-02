package com.zeimyth.controllers.api

import com.zeimyth.controllers.ChatController
import com.zeimyth.models.AccountModel
import com.zeimyth.models.ConnectionModel
import com.zeimyth.models.UserInfo

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._

import play.api.Play

import play.Logger
import play.api.libs.json.JsValue
import com.zeimyth.views.api.json.Message

object ConnectionApiController extends ChatController {

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
				BadRequest("login usage info (TEMP)")
			},
			user => {
				val id = request.cookies.get("connectionId") match {
					case Some(cookie) => cookie.value.toLong
					case None => -1L
				}

				Logger.trace("Received login from " + request.remoteAddress + " (" + id + "): (" + user.username + ", " +
					user.password + ")")

				Ok("")
			}
		)

	}

	def logout = Action { implicit request =>
		Logger.trace("Received logout from " + request.remoteAddress)
		Ok("")
	}

	def create = Action(parse.json) (
		withConnection[JsValue] { request =>
			userForm.bindFromRequest()(request.getRequest).fold(
				formWithErrors => {
					Logger.trace("Received malformed create request from " + request.remoteAddress + ", reason: " +
						formWithErrors.errors)
					BadRequest("create usage info (TEMP)")
				},
				user => {
					Logger.trace("Received create request from " + request.info + ": (" + user.username + ", " + user.password +
						")")

					if (AccountModel.newAccount(user.username, user.password).isDefined) {
						Ok(Message("Account " + user.username + " created (TEMP)"))
					}
					else {
						Logger.trace("An error occurred during account creation")
						BadRequest(Message("username in use or password invalid (TEMP)"))
					}
				}
			)
		}
	)
}