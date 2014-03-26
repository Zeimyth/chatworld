package com.zeimyth.controllers.api

import com.zeimyth.controllers.ChatController
import com.zeimyth.controllers.CustomRequest.extractRequest

import play.api.mvc.Action
import com.zeimyth.models.AccountModel
import com.zeimyth.views.api.json.who.WhoDisplay

object IdentityController extends ChatController {

	def who = Action(parse.empty) (
		withConnection { implicit request =>
			val query = request.getQueryString("names")

			val names = query match {
				case Some(nameList) => nameList.split(",").toSeq
				case None => Seq()
			}

			val whoData = if (names.isEmpty) {
				names.map(AccountModel.whois)
					.filter(_.isDefined)
					.map(_.get)
			}
			else {
				AccountModel.whoisOnline()
			}

			Ok(WhoDisplay(whoData))
		}
	)

	def whoAmI = Action(parse.empty) (
		withLogin { implicit request =>
			val name = request.getUsername
			val whoData = Seq(AccountModel.whois(name))
				.filter(_.isDefined)
				.map(_.get)

			Ok(WhoDisplay(whoData))
		}
	)
}
