package com.zeimyth.views.api.json.connection

import com.zeimyth.views.api.json.Message

object Logout {
	def apply() = {
		Message.good("Successfully logged out.")
	}
}
