package com.zeimyth.controllers.api

import com.zeimyth.controllers.ChatController
import com.zeimyth.controllers.FormUtils.formWrapper
import com.zeimyth.models.AccountModel
import com.zeimyth.utils.ListenManager
import com.zeimyth.utils.MessageType._
import com.zeimyth.views.api.json.{Default, Empty}

import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Action
import play.Logger
import play.api.libs.json.Json

object CommunicationApiController extends ChatController {

	case class input(text: String)

//	case class location()
//	case class listenRequest(roomId: Long)

	val inputForm = Form(
		mapping(
			"text" -> text
		)(input.apply)(input.unapply)
	)

	def say = Action(parse.json) (
		withLogin { implicit request =>
			val input = inputForm.bindFromCustomRequest.get
			Logger.trace("Received say from " + request.info + ": " + input.text)

			ListenManager.addMessage(input.text, request.connectionId, Say)
			Ok(Empty())
		}
	)

	def emote = Action(parse.json) (
		withLogin { implicit request =>
			val input = inputForm.bindFromCustomRequest.get
			Logger.trace("Received emote from " + request.info + ": " + input.text)

			ListenManager.addMessage(input.text, request.connectionId, Emote)
			Ok(Empty())
		}
	)

	def listen = Action(parse.json) (
		withConnection { implicit request =>
			Logger.trace("Received listen request from " + request.info + ".")
			AccountModel.getAccountByConnectionId(request.connectionId) match {
				case Some(_) =>
					val result = ListenManager.listen(request.connectionId)
					Logger.trace(result.toString())
					Ok(result)
				case None => Ok(Default(true, Json.parse("{}")))
			}
		}
	)
}