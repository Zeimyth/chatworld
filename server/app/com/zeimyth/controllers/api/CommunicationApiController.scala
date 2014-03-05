package com.zeimyth.controllers.api

import com.zeimyth.controllers.ChatController
import com.zeimyth.controllers.FormUtils.formWrapper
import com.zeimyth.utils.ListenManager
import com.zeimyth.utils.MessageType._

import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Action
import play.Logger

object CommunicationApiController extends ChatController {

	case class input(text: String)

//	case class location()
//	case class listenRequest(roomId: Long)

	val inputForm = Form(
		mapping(
			"text" -> text
		)(input.apply)(input.unapply)
	)

//	val listenRequestForm = Form(
//		mapping(
//			"room" -> longNumber
//		)(listenRequest.apply)(listenRequest.unapply)
//	)

	def say = Action(parse.json) (
		withLogin { implicit request =>
			val input = inputForm.bindFromCustomRequest.get
			Logger.trace("Received say from " + request.info + ": " + input.text)

			ListenManager.addMessage(input.text, request.connectionId, Say)
			Ok("")
		}
	)

	def listen = Action(parse.json) (
//		withLogin { implicit request =>
		withConnection { implicit request =>
//			val request = listenRequestForm.bindFromCustomRequest.get
			Logger.trace("Received listen request from " + request.info + ".")

			val result = ListenManager.listen(request.connectionId)
			Logger.trace(result.toString())
			Ok(result)
		}
	)
}