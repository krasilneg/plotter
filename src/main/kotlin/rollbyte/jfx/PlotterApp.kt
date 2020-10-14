package rollbyte.jfx

import rollbyte.jfx.views.MainView
import javafx.application.Application
import tornadofx.App

class PlotterApp: App(MainView::class, Styles::class)

/**
 * The main method is needed to support the mvn jfx:run goal.
 */
fun main(args: Array<String>) {
    Application.launch(PlotterApp::class.java, *args)
}
