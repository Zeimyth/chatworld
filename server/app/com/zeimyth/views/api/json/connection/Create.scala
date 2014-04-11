package com.zeimyth.views.api.json.connection

import com.zeimyth.views.api.json.Message

object Create {
	def success = {
		Message.good("New account created successfully!")
	}

	def invalid(reason: String) = {
		Message.bad("Unable to create a new account; " + reason)
	}
}
