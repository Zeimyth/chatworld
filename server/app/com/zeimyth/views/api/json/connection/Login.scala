package com.zeimyth.views.api.json.connection

import com.zeimyth.views.api.json.Message

object Login {
	def success = {
		Message.good("Logging in...")
	}

	def invalid = {
		Message.bad("Invalid username or password.")
	}
}
