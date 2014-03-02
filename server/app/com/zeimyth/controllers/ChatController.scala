package com.zeimyth.controllers

import play.api.mvc.{Action, Result, Request, Controller}
import com.zeimyth.models.{ConnectionModel, Connection}

trait ChatController extends Controller {
	def withConnection[A](inner: CustomRequest[A] => Result)(request: Request[A]): Result = {
		request.cookies.get("connectionId") match {
			case Some(connectionCookie) =>
				ConnectionModel.getConnection(connectionCookie.value.toLong) match {
					case Some(connection) => inner(new CustomRequest[A](request, connection))
					case None => Unauthorized("Invalid connection id (TEMP)")
				}
			case None => Unauthorized("No connection id (TEMP)")
		}
	}
}