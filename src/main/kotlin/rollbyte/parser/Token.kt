package rollbyte.parser

class Token(t: TokenType, v: String? = null) {
  var type = t
  var value: String = v ?: (
    when (t) {
      TokenType.LEFT_PARENTHESIS -> "("
      TokenType.RIGHT_PARENTHESIS -> ")"
      TokenType.ARG_SEPARATOR -> ","
      else -> ""
    }
  )
}