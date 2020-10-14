package rollbyte.web

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.annotation.WebServlet
import javax.json.Json
import javax.imageio.ImageIO
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.scene.image.WritableImage
import javafx.embed.swing.SwingFXUtils
import org.apache.commons.codec.binary.Base64
import rollbyte.model.Calculator
import rollbyte.parser.Parser
import rollbyte.model.Wolfram
import rollbyte.plotter.plot

@WebServlet(name = "Index", value = ["/"])
class Index: HttpServlet() {
  override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
    val stream = servletContext.getResourceAsStream("/WEB-INF/classes/index.html")
    res.contentType = "text/html"
    stream!!.copyTo(res.outputStream)
  }

  override fun doPost(req: HttpServletRequest, res: HttpServletResponse) {
    val data = Json.createReader(req.reader).readObject()
    val parser = data.getString("parser")
    try {
      if (parser == "native") {
        val plotSize = 300.0
        val calc = Calculator(Parser())
        val values = calc.process(data.getString("expr"), data.getString("from").toDouble(), data.getString("to").toDouble(), plotSize / 3)
        val img = plot(values, plotSize, 20.0, data.getBoolean("realAxis"))
        val baos = ByteArrayOutputStream()
        ImageIO.write(img, "png", baos)
        res.writer.write("data:image/png;base64," + String(Base64.encodeBase64(baos.toByteArray()), StandardCharsets.UTF_8))
      } else {
        val wolframAppId = System.getenv("WFA_ID") ?: ""
        val wf = Wolfram(wolframAppId)
        val url = wf.plot(data.getString("expr"), data.getString("from").toDouble(), data.getString("to").toDouble())
        res.writer.write(url)
      }
    } catch (e: Exception) {
      res.setStatus(500)
      res.writer.write(e.message)
      for (el in e.stackTrace) {
        res.writer.write("${el.fileName} ${el.lineNumber}: ${el.toString()}")
      }
    }    
  }
}