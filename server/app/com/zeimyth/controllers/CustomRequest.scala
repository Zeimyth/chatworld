package com.zeimyth.controllers

import play.api.mvc.Request
import com.zeimyth.models.{AccountModel, ConnectionModel, Connection}

object CustomRequest {
	implicit def extractRequest(cRequest: CustomRequest): Request[_] = cRequest.getRequest
}

class CustomRequest(request: Request[_], connection: Connection) {
	lazy val getRequest = request
	lazy val remoteAddress = request.remoteAddress

	lazy val getConnection = connection
	lazy val connectionId = connection.id

	lazy val info = remoteAddress + " (" + connectionId +
		(AccountModel.getAccountByConnectionId(connectionId) match {
			case Some(account) => ", " + account.username
			case None => ""
		}) + ")"
}