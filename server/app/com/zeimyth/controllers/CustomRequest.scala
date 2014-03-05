package com.zeimyth.controllers

import play.api.mvc.Request
import com.zeimyth.models.Connection

object CustomRequest {
	implicit def extractRequest(cRequest: CustomRequest): Request[_] = cRequest.getRequest
}

class CustomRequest(request: Request[_], connection: Connection) {
	lazy val getRequest = request
	lazy val remoteAddress = request.remoteAddress

	lazy val getConnection = connection
	lazy val connectionId = connection.id

	lazy val info =  s"$remoteAddress ($connectionId)"
}