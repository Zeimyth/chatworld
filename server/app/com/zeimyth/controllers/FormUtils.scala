package com.zeimyth.controllers

import play.api.data.Form

object FormUtils {
	implicit def formWrapper[A](form: Form[A]) = new FormUtils(form)
}

class FormUtils[A](form: Form[A]) {
	def bindFromCustomRequest(implicit request: CustomRequest) = form.bindFromRequest()(request.getRequest)
}
