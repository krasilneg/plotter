package rollbyte.parser

interface ParserInterface {
  fun parse(expression: String): (x: Double) -> Double
}