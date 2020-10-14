package rollbyte.model

import rollbyte.parser.ParserInterface

class Calculator(p: ParserInterface) {
  private var f: (x: Double) -> Double = {x: Double -> x}
  private var last: String = ""
  private var parser = p

  fun process(expression: String, from: Double, to: Double, scale: Double): Graphic {
    if (expression != last) {
      f = parser.parse(expression)
    }
    val result = Graphic()
    val st = (to - from)/scale
    var x = from
    var y: Double
    while (x < to) {
      y = f(x)
      if (!y.isNaN()) {
        result.put(x, y)
      }
      x += st
    }
    y = f(to)
    if (!y.isNaN()) {
      result.put(to, y)
    }
    return result
  }
}