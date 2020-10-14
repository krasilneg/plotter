package rollbyte.jfx.views

import rollbyte.jfx.Styles
import rollbyte.model.Calculator
import rollbyte.parser.Parser
import rollbyte.model.Wolfram
import rollbyte.plotter.plot
import javafx.embed.swing.SwingFXUtils
import javafx.scene.control.Alert.AlertType.ERROR
import javafx.scene.control.TextFormatter
import javafx.scene.control.ToggleGroup
import javafx.scene.image.Image
import javafx.scene.canvas.Canvas
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
                                try {
                                    if (to.value < from.value)
                                        throw Exception("Invalid plot range")
                                    val gc = graph.getGraphicsContext2D()
                                    gc.setFill(Color.WHITE)
                                    gc.fillRect(0.0, 0.0, graph.width, graph.height)                                 
                                    if (parserType.value == "native") {                            
                                        val calc = Calculator(Parser())
                                        val values = calc.process(expr.value, from.value, to.value, PLOT_SIZE / 2)
                                        val img = plot(values, PLOT_SIZE, PADDING, realAxis.value)
                                        gc.drawImage(SwingFXUtils.toFXImage(img, null), 0.0, 0.0)
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