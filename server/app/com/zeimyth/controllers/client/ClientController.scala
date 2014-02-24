package com.zeimyth.controllers.client

import play.api.mvc.{Action, Controller}

object ClientController extends Controller {

	def index = Action {
		Ok(com.zeimyth.views.html.client.index())
	}
}