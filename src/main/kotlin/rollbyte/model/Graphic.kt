package rollbyte.model

class Graphic {
  val data: MutableMap<Double, Double> = mutableMapOf()
  private var min: Double? = null
  private var max: Double? = null
  val minimum: Double?
      get() = min
  val maximum: Double?
      get() = max

  fun put(x: Double, y: Double) {
    data.put(x, y)
    if (min == null || y < min!!) min = y
    if (max == null || y > max!!) max = y
  }
}