package com.zeimyth.controllers.client

case class Topic(name: String, render: String)

object HelpController {
	val topics = scala.collection.mutable.Map[String, Topic]()
	val default = "I don't understand that (Type \"help\" for help)"

	def getTopicText(topic: String): String = {
		try {
			topics(topic).render
		}
		catch {
			case e: NoSuchElementException => default
		}
	}
}