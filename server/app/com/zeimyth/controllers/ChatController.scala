package com.zeimyth.controllers

import com.zeimyth.models.{AccountModel, ConnectionModel}
import com.zeimyth.views.api.json.Message

import play.api.mvc.{Cookie, Result, Request, Controller}

trait ChatController extends Controller {
	def withConnection(inner: CustomRequest => Result)(request: Request[_]): Result = {
		request.cookies.get("connectionId") match {
			case Some(connectionCookie) =>
				ConnectionModel.getConnection(connectionCookie.value.toLong) match {
					case Some(connection) => inner(new CustomRequest(request, connection))
					case None =>
						// Somehow notify user that their connection was reset?
						Unauthorized("Invalid connection id (TEMP)")
							.withCookies(Cookie("connectionId", ConnectionModel.newConnection().toString))
				}
			case None =>
				// Somehow notify user that their connection was reset?
				Unauthorized("No connection id (TEMP)")
					.withCookies(Cookie("connectionId", ConnectionModel.newConnection().toString))
		}
	}

	def withLogin(inner: CustomRequest => Result)(request: Request[_]): Result = {
		withConnection { cRequest =>
			AccountModel.getAccountByConnectionId(cRequest.connectionId) match {
				case Some(account) => inner(cRequest)
				case None => Unauthorized(Message("You must be logged in to do that (TEMP)"))
			}
		}(request)
	}
}