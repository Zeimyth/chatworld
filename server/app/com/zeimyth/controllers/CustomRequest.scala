package com.zeimyth.controllers

import com.zeimyth.models.{ConnectionModel, AccountModel, Connection}

import play.api.mvc.Request

object CustomRequest {
	implicit def extractRequest(cRequest: CustomRequest): Request[_] = cRequest.getRequest
}

class CustomRequest(request: Request[_], connection: Connection) {
	lazy val getRequest = request
	lazy val remoteAddress = request.remoteAddress

	lazy val getConnection = connection
	lazy val connectionId = connection.id

	lazy val getAccount = AccountModel.getAccountByConnectionId(connectionId)
	lazy val getUsername = getAccount match {
		case Some(account) => account.username
		// This space intentionally left blank - We should not be looking for
		// the username if they are not logged in
		// This could become a part of a subclass for the CustomRequest class, somehow
	}

	lazy val info = AccountModel.getAccountByConnectionId(connectionId) match {
		case Some(account) => account.info + " (cId=" + connectionId + ",ip=" + remoteAddress + ")"
		case None => remoteAddress + " (cId=" + connectionId + ")"
	}

	def resetIdleTimer() {
		ConnectionModel.resetIdleTimer(connection)
	}
}