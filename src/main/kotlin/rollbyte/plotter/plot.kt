package rollbyte.plotter

import rollbyte.model.Graphic
import java.awt.BasicStroke
import java.awt.Color
import java.awt.image.BufferedImage


fun plot(values: Graphic, plotSize: Double, padding: Double, realAxis: Boolean = false): BufferedImage {
  val scaleX = values.to!! - values.from!!;                           
  val scaleY = values.maximum!! - values.minimum!!
  val fullSize = plotSize + 2 * padding

  var kx = 1.0
  var ky = 1.0
  if (realAxis) {
    if (scaleY > scaleX) {
      kx = scaleX / scaleY
    } else if (scaleY < scaleX) {
      ky = scaleY / scaleX
    }
  }

  val img = BufferedImage(fullSize.toInt(), fullSize.toInt(), BufferedImage.TYPE_INT_RGB);
  val gc = img.createGraphics()

  gc.paint = Color.WHITE
  gc.fillRect(0, 0, fullSize.toInt(), fullSize.toInt())

  gc.stroke = BasicStroke(1.toFloat())
  gc.paint = Color.LIGHT_GRAY

  if (values.from!! < 0) {
    val zeroX = padding - values.from!! * plotSize * kx / scaleX
    gc.drawLine(zeroX.toInt(), padding.toInt(), zeroX.toInt(), plotSize.toInt());
  }

  if (values.minimum!! < 0) {
    val zeroY = padding + plotSize - (-values.minimum!! * plotSize * ky/ scaleY)
    gc.drawLine(padding.toInt(), zeroY.toInt(), plotSize.toInt(), zeroY.toInt());
  }
  
  gc.paint = Color.BLACK
  gc.drawString(String.format("%.3f", values.maximum!!), padding.toInt(), padding.toInt() + 20)
  gc.drawString(String.format("%.3f", values.minimum!!), padding.toInt(), (padding + plotSize * ky).toInt())

  gc.stroke = BasicStroke(2.toFloat())
  gc.paint = Color.RED

  var prev: Map.Entry<Double, Double>?  = null
  values.data.entries.forEach({p ->
    if (prev != null) {
      val x0: Double = padding + (prev!!.key - values.from!!) * plotSize * kx / scaleX
      val x1: Double = padding + (p.key - values.from!!) * plotSize * kx / scaleX
      val y0: Double = padding + plotSize - (prev!!.value - values.minimum!!) * plotSize * ky / scaleY
      val y1: Double = padding + plotSize - (p.value - values.minimum!!) * plotSize * ky / scaleY
      gc.drawLine(x0.toInt(), y0.toInt(), x1.toInt(), y1.toInt())
    }
    prev = p
  })
  return img
}