package rollbyte.views

import rollbyte.Styles
import rollbyte.model.Calculator
import rollbyte.parser.Parser
import rollbyte.model.Wolfram
import javafx.scene.control.Alert.AlertType.ERROR
import javafx.scene.control.TextFormatter
import javafx.scene.control.ToggleGroup
import javafx.scene.image.Image
import javafx.scene.canvas.*
import javafx.scene.paint.Color
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.util.StringConverter
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale
import tornadofx.*

class MainView : View("Plotter") {
    companion object{
        private const val PLOT_SIZE: Double = 480.0
        private const val PADDING: Double = 10.0
    }
    private val expr = SimpleStringProperty()
    private val from = SimpleDoubleProperty()
    private val to = SimpleDoubleProperty()
    private val parserType = SimpleStringProperty("native")
    private val realAxis = SimpleBooleanProperty(false)
    private val parserTypeGroup = ToggleGroup()
    private var graph: Canvas by singleAssign()
    private val wolframAppId = System.getenv("WFA_ID") ?: ""

    private val format = NumberFormat.getInstance(Locale.getDefault())
    
    private val numConverter = object : StringConverter<Number>() {
        override fun toString(number: Number?) = if (number != null) format.format(number) else ""
        override fun fromString(string: String?) = format.parse(string).toDouble()
    }

    private val numMask = fun(it: TextFormatter.Change): Boolean {
        try { format.parse(it.controlNewText).toDouble() } catch (e: ParseException) {return false}
        return true
    }

    override val root = borderpane {
        addClass(Styles.window)
        center {
            vbox {
                addClass(Styles.content)
                form {
                    label("Function")
                    textarea(expr)
                    hbox {
                        addClass(Styles.hrow)
                        label("Plot range")
                        textfield(from, numConverter) {
                            filterInput(numMask)
                        }
                        textfield(to, numConverter) {
                            filterInput(numMask)
                        }
                    }
                    hbox {
                        addClass(Styles.hrow)
                        radiobutton("Native parser", parserTypeGroup, "native")
                        radiobutton("Wolfram alpha parser", parserTypeGroup, "wolfram")
                    }
                    hbox {
                        addClass(Styles.hrow)
                        checkbox("real axis scale", realAxis) {
                            style {
                                spacing = 15.px
                            }
                        }
                        button("Plot") {
                            action {
                                var gc = graph.getGraphicsContext2D()
                                gc.setFill(Color.WHITE)
                                gc.fillRect(0.0, 0.0, graph.getWidth(), graph.getHeight())                            
                                try {
                                    val scaleX = to.value - from.value;
                                    if (scaleX <= 0)
                                        throw Exception("Invalid plot range")
                                    if (parserType.value == "native") {                            
                                        val calc = Calculator(Parser())
                                        val values = calc.process(expr.value, from.value, to.value, PLOT_SIZE / 3)
                                        val scaleY = values.maximum!! - values.minimum!!

                                        var kx = 1.0
                                        var ky = 1.0
                                        if (realAxis.value) {
                                            if (scaleY > scaleX) {
                                                kx = scaleX / scaleY
                                            } else if (scaleY < scaleX) {
                                                ky = scaleY / scaleX
                                            }
                                        }

                                        gc.setLineWidth(1.0)
                                        gc.setStroke(Color.SILVER)
    
                                        if (from.value < 0) {
                                            val zeroX = PADDING-from.value * PLOT_SIZE * kx / scaleX
                                            gc.strokeLine(zeroX, PADDING, zeroX, PLOT_SIZE);
                                        }
        
                                        if (values.minimum!! < 0) {
                                            val zeroY = PADDING + PLOT_SIZE - (-values.minimum!! * PLOT_SIZE * ky/ scaleY)
                                            gc.strokeLine(PADDING, zeroY, PLOT_SIZE, zeroY);
                                        }
                                        gc.setStroke(Color.BLACK)
                                        gc.strokeText(String.format("%.3f", values.maximum!!), PADDING, PADDING + 20.0)
                                        gc.strokeText(String.format("%.3f", values.minimum!!), PADDING, PADDING + PLOT_SIZE * ky)

                                        gc.setLineWidth(2.0)
                                        gc.setStroke(Color.RED)
                                        gc.beginPath()
                                        var prev: Map.Entry<Double, Double>?  = null
                                        values.data.entries.forEach({p ->
                                            if (prev != null) {
                                                val x0: Double = PADDING + (prev!!.key - from.value) * PLOT_SIZE * kx / scaleX
                                                val x1: Double = PADDING + (p.key - from.value) * PLOT_SIZE * kx / scaleX
                                                val y0: Double = PADDING + PLOT_SIZE - (prev!!.value - values.minimum!!) * PLOT_SIZE * ky / scaleY
                                                val y1: Double = PADDING + PLOT_SIZE - (p.value - values.minimum!!) * PLOT_SIZE * ky / scaleY
                                                gc.quadraticCurveTo(x0, y0, x1, y1)
                                            } else {
                                                val x1: Double = PADDING + (p.key - from.value) * PLOT_SIZE * kx / scaleX
                                                val y1: Double = PADDING + PLOT_SIZE - (p.value - values.minimum!!) * PLOT_SIZE * ky / scaleY
                                                gc.moveTo(x1, y1)
                                            }
                                            prev = p
                                        })
                                        gc.stroke()
                                    } else {
                                        val wf = Wolfram(wolframAppId)
                                        val url = wf.plot(expr.value, from.value, to.value)
                                        val img = Image(url)
                                        gc.drawImage(img, PADDING, PADDING)
                                    }
                                } catch (e: Exception) {
                                    alert(ERROR, "something went wrong", e.message ?: e.localizedMessage)
                                }
                            }
                        }
                    }
                }
                canvas(PLOT_SIZE + PADDING * 2, PLOT_SIZE + PADDING * 2) {
                    graph = this
                }
            }
        }
    }

    init {
        parserTypeGroup.bind(parserType)
    }
}