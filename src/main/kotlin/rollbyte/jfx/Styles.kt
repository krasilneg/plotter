package rollbyte.jfx

import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val window by cssclass()
        val content by cssclass()
        val hrow by cssclass()
    }

    init {
        window {
            padding = box(10.px)
            content {
                padding = box(15.px)
                button {
                    fontSize = 14.px
                }
                hrow {
                    padding = box(10.px)
                    spacing = 15.px
                }
            }
        }
    }
}