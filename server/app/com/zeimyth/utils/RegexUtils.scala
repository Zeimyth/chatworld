package com.zeimyth.utils

import scala.util.matching.Regex

object RegexUtils {
	implicit def regexToRichRegex(r: Regex) = new RichRegex(r)
}

class RichRegex(underlying: Regex) {
	def matches(s: String) = underlying.pattern.matcher(s).matches
}
