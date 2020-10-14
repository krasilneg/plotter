package rollbyte.model

import com.wolfram.alpha.WAEngine
import com.wolfram.alpha.WAImage

class Wolfram(appId: String) {
  private var engine = WAEngine()

  init {
    if (appId.length == 0) {
      throw Exception("No wolfram application ID specified")
    }
    engine.appID = appId
  }

  fun plot(expression: String, from: Double, to: Double, real: Boolean = false): String { 
    val cmd = if (real) "real plot" else "plot"
    val q = engine.createQuery("$cmd " + expression + " from x=" + from + " to " + to)
    q.addIncludePodID("Plot")
    q.addFormat("image")
    //q.addFormat("moutput")
    val res = engine.performQuery(q)
    if (res.numPods == 0) {
      throw Exception("No result pods obtained from API. Check your input or try later.")
    }
    return (res.pods[0].subpods[0].contents[0] as WAImage).getURL()
  }
}