package com.zeimyth.models

import com.zeimyth.models.migrations._
import play.api.db.DB
import play.api.Play.current

object PatchManager {

	var patches = false

	val patchList: List[Patch] = List(
		new Patch001
	)

	def ensurePatches() {
		if (!patches) {
			runPatches()
			patches = true
		}
	}

	private def runPatches() {
		DB.withConnection { conn =>
			patchList.foreach(_.execute(conn))
		}
	}

	private def undoPatches() {
		DB.withConnection { conn =>
			patchList.reverse.foreach(_.unexecute(conn))
		}
	}
}