package rollbyte.parser

class Operator(op: (a1: Double, a2: Double) -> Double, prec: Int, assoc: Assoc = Assoc.LEFT) {
  var precedence = prec
  var logic = op
  var associate = assoc
}