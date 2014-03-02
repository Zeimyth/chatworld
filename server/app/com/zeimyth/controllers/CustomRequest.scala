package com.zeimyth.controllers

import play.api.mvc.Request
import com.zeimyth.models.Connection

class CustomRequest[A](request: Request[A], connection: Connection) {
	lazy val getRequest = request
	lazy val remoteAddress = request.remoteAddress

	lazy val connectionId = connection.id

	lazy val info =  s"$remoteAddress ($connectionId)"
}