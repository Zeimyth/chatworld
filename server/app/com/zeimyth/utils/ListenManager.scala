package com.zeimyth.utils

import com.zeimyth.models.AccountModel
import com.zeimyth.utils.MessageType._
import com.zeimyth.views.api.json.Default

import play.api.libs.json._

import scala.Some

case class Message(content: String, source: Long, code: MessageType)
case class Listener(id: Long, messageIdx: Long/*, messageBank: Long*/)

@volatile
object ListenManager {
	var messageList = List[Message]()
	val listenerMap = scala.collection.mutable.Map[Long, Listener]()

	def addMessage(content: String, source: Long, code: MessageType/*, room: Long*/) {
//		messageList :: message
		messageList = messageList :+ Message(content, source, code)
	}

	def listen(listenerId: Long): JsValue = {
		Default(true, Json.toJson(Map(
			"messages" -> (
				listenerMap.get(listenerId) match {
					case Some(listener) =>
						getNewMessages(listener)
					case None =>
						initializeNewListener(listenerId)
						JsNull
				}
			)
		)))
	}

	private def initializeNewListener(listenerId: Long) {
		listenerMap += (listenerId -> Listener(listenerId, messageList.size))
	}

	private def getNewMessages(listener: Listener) = {
		val result = Json.toJson(messageList.view.zipWithIndex
			.filter(messageWithIdx => messageWithIdx._2 >= listener.messageIdx)
			.map(messageWithIdx => messageToJs(messageWithIdx._1, listener.id))
		)

		listenerMap += (listener.id -> listener.copy(messageIdx = messageList.size))
		result
	}

	private def messageToJs(message: Message, listenerId: Long) = {
		message.code match {
			case Say =>
				val pre = if (message.source == listenerId) {
					"You say, "
				}
				else {
					getNameOfSource(message.source) + " says, "
				}

				Json.toJson(Map("text" -> Json.toJson(pre + "\"" + message.content + "\""),
												"type" -> Json.toJson(Say.toString)))
		}
	}

	private def getNameOfSource(source: Long) = {
		AccountModel.getAccount(source) match {
			case Some(account) => account.username
			case None => "It"
		}
	}
}
