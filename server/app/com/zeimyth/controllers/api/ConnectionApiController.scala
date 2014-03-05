package com.zeimyth.controllers.api

import com.zeimyth.controllers.ChatController
import com.zeimyth.controllers.FormUtils.formWrapper
import com.zeimyth.models.AccountModel
import com.zeimyth.models.ConnectionModel
import com.zeimyth.models.UserInfo

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._

import play.api.Play

import play.Logger
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

	def disconnect = Action(parse.empty) (
		withConnection { implicit request =>
			Logger.trace("Received disconnect from " + request.info)
			ConnectionModel.closeConnection(request.connectionId)

			Ok("")
		}
	)

	def login = Action(parse.json) (
		withConnection { implicit request =>
			userForm.bindFromCustomRequest.fold(
				formWithErrors => {
					Logger.trace("Received malformed login request from " + request.remoteAddress)
					BadRequest("login usage info (TEMP)")
				},
				user => {
					Logger.trace("Received login from " + request.info + ": (" + user.username + ", " + user.password + ")")

					if (AccountModel.tryLogin(user, request.getConnection)) {
						Ok(Message("login successful! (TEMP)"))
					}
					else {
						BadRequest("invalid login (TEMP)")
					}
				}
			)
		}
	)

	def logout = Action(parse.empty) (
		withLogin { implicit request =>
			Logger.trace("Received logout from " + request.info)
			ConnectionModel.getUserIdByConnection(request.connectionId) match {
				case Some(userId) => AccountModel.logout(userId)
				case None =>
			}
			Ok(Message("logged out (TEMP)"))
		}
	)

	def create = Action(parse.json) (
		withConnection { implicit request =>
			userForm.bindFromCustomRequest.fold(
				formWithErrors => {
					Logger.trace("Received malformed create request from " + request.remoteAddress + ", reason: " +
						formWithErrors.errors)
					BadRequest("create usage info (TEMP)")
				},
				user => {
					Logger.trace("Received create request from " + request.info + ": (" + user.username + ", " + user.password +
						")")

					AccountModel.newAccount(user.username, user.password) match {
						case Some(account) =>
							AccountModel.login(account, request.getConnection)
							Ok(Message("Account " + user.username + " created and logged in (TEMP)"))
						case None =>
							Logger.trace("An error occurred during account creation")
							BadRequest(Message("username in use or password invalid (TEMP)"))
					}
				}
			)
		}
	)
}