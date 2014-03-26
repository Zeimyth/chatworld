package com.zeimyth.views.api.json.who

import com.zeimyth.models.Account
import com.zeimyth.views.api.json.Message

object WhoDisplay {
	def apply(accounts: Seq[Account]) = {
		Message(accounts.map(_.info).mkString("\n"))
	}
}
