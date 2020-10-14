package rollbyte.parser

class Function(l: (args: DoubleArray) -> Double, n: Int = 1) {
  var logic = l
  var args = n
}