import kotlin.test.assertEquals
import kotlin.test.Test
import rollbyte.parser.Parser
import rollbyte.parser.Function
import rollbyte.parser.Operator
import kotlin.math.pow

class ParserTest {
/* 
  @Test
  fun testArithmetic() {
    val p = Parser()
    val f = p.parse("2^3 + 5*4/2 - 10")
    assertEquals(8.0 ,f(0.0))    
  }

  @Test
  fun testUnar() {
    val p = Parser()
    val f = p.parse("-2^3 + 5*4/2 - 10")
    assertEquals(-8.0 ,f(0.0))    
  }

  @Test
  fun testVariable() {
    val p = Parser()
    val f = p.parse("-x^3 + 5*4/x - 10")
    assertEquals(-8.0 ,f(2.0))
  }

  @Test
  fun testFunctions() {
    val p = Parser()
    val f = p.parse("-x^2 + 5*4/x + sqrt(x) - 10")
    assertEquals(-19.0 ,f(4.0))
  }
*/
  @Test
  fun testExtensions() {
    val p = Parser(
      extBo = mapOf("%" to Operator({ a: Double, b: Double -> a % b }, 2)),
      extUo = mapOf("&" to { a -> a.pow(2) }),
      extFunc = mapOf("sum" to Function({args: DoubleArray -> args[0] + args[1] + args[2]}, 3))
    )
    val f = p.parse("&x%3 - sum(x, 3, x)")
    assertEquals(-15.0 ,f(8.0))
  }
}