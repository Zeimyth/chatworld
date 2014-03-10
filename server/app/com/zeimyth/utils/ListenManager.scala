package com.zeimyth.utils

import com.zeimyth.models.AccountModel
import com.zeimyth.utils.MessageType._
import com.zeimyth.views.api.json.Default

import play.api.libs.json._
import play.Logger

import scala.Some

case class Message(content: String, source: Long, code: MessageType)
case class Listener(id: Long, messageIdx: Long/*, messageBank: Long*/)

@volatile
object ListenManager {
	var messageList = List[Message]()
	val listenerMap = scala.collection.mutable.Map[Long, Listener]()

	def addMessage(content: String, source: Long, code: MessageType/*, room: Long*/) {
		val newMessage = Message(content, source, code)
		Logger.info(getNameOfSource(source) + ": " + getMessageText(newMessage, -1L))
		messageList = messageList :+ newMessage
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
			.filter(messageWithIdx => messageWithIdx._2 >= listener.messageIdx && willSeeMessage(messageWithIdx._1, listener))
			.map(messageWithIdx => messageToJs(messageWithIdx._1, listener.id))
		)

		listenerMap += (listener.id -> listener.copy(messageIdx = messageList.size))
		purgeMessages()
		result
	}

	private def messageToJs(message: Message, listenerId: Long) = {
		Json.obj("text" -> Json.toJson(getMessageText(message, listenerId)),
			       "type" -> Json.toJson(message.code.toString))
	}

	private def getMessageText(message: Message, listenerId: Long) = {
		message.code match {
			case Say =>
				val pre = if (message.source == listenerId) {
					"You say, "
				}
				else {
					getNameOfSource(message.source) + " says, "
				}

				pre + "\"" + message.content + "\""

			case Emote =>
				getNameOfSource(message.source) + " " + message.content

			case Login =>
				message.content
		}
	}

	private def willSeeMessage(message: Message, listener: Listener) = {
		message.code match {
			case Say => true
			case Emote => true
			case Login => message.source != listener.id
		}
	}

	private def getNameOfSource(source: Long) = {
		AccountModel.getAccount(source) match {
			case Some(account) => account.username
			case None => "It"
		}
	}

	private def purgeMessages() {
		val minIdx = listenerMap.foldRight(messageList.length.toLong) { (listenerMapEntry, min) =>
			Math.min(min, listenerMapEntry._2.messageIdx)
		}

		if (minIdx > 0) {
			messageList = messageList.view.zipWithIndex
				.filter(messageWithIdx => messageWithIdx._2 >= minIdx)
				.map(messageWithIdx => messageWithIdx._1)
				.toList

			listenerMap.foreach{listenerMapEntry =>
				val listener = listenerMapEntry._2
				listenerMap += (listener.id -> listener.copy(messageIdx = listener.messageIdx - minIdx))
			}
		}
	}

	private def debugMessageBank() {
		Logger.trace("Messages: " + messageList.length)
		messageList.foreach(message => Logger.trace(message.toString))
	}
}
