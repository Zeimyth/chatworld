package com.zeimyth.controllers

import play.api.mvc.{Action, Result, Request, Controller}
import com.zeimyth.models.{AccountModel, ConnectionModel, Connection}
import com.zeimyth.views.api.json.Message

trait ChatController extends Controller {
	def withConnection(inner: CustomRequest => Result)(request: Request[_]): Result = {
		request.cookies.get("connectionId") match {
			case Some(connectionCookie) =>
				ConnectionModel.getConnection(connectionCookie.value.toLong) match {
					case Some(connection) => inner(new CustomRequest(request, connection))
					case None => Unauthorized("Invalid connection id (TEMP)")
				}
			case None => Unauthorized("No connection id (TEMP)")
		}
	}

	def withLogin(inner: CustomRequest => Result)(request: Request[_]): Result = {
		withConnection { cRequest =>
			cRequest.getConnection.userId match {
				case Some(userId) =>
					if (AccountModel.getAccount(userId).isDefined) {
						inner(cRequest)
					}
					else {
						Unauthorized(Message("Login verification error (TEMP)"))
					}
				case None => Unauthorized(Message("You must be logged in to do that (TEMP)"))

			}
		}(request)
	}
}