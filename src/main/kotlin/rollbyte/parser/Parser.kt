package rollbyte.parser

import kotlin.math.pow
import kotlin.math.sqrt
import java.util.Stack
import kotlin.text.toDouble

typealias Predicate = (a: Double) -> Double

class Parser (
    extBo: Map<String, Operator>? = null,
    extFunc: Map<String, Function>? = null,
    extUo: Map<String, Predicate>?  = null
): ParserInterface {
  private val bo = mutableMapOf<String, Operator>(
    "+" to Operator({a1: Double, a2: Double -> a1 + a2}, 1),
    "-" to Operator({a1: Double, a2: Double -> a1 - a2}, 1),
    "*" to Operator({a1: Double, a2: Double -> a1 * a2}, 2),
    "/" to Operator({a1: Double, a2: Double -> a1 / a2}, 2),
    "^" to Operator({a1: Double, a2: Double -> a1.pow(a2)}, 3, Assoc.RIGHT)
  )

  private val uo = mutableMapOf<String, Predicate>(
    "-" to {a: Double -> -a}
  )

  private val func = mutableMapOf<String, Function>(
    "sqrt" to Function({args: DoubleArray -> sqrt(args[0])})
  )

  init {
    if (extBo != null) {
      bo.putAll(extBo)
    }
    if (extFunc != null) {
      func.putAll(extFunc)
    }
    if (extUo != null) {
      uo.putAll(extUo)
    }
  }

  private fun type(token: String, unar: Boolean = false): TokenType {
    if (token == ",") return TokenType.ARG_SEPARATOR
    if (token == "(") return TokenType.LEFT_PARENTHESIS
    if (token == ")") return TokenType.RIGHT_PARENTHESIS
    if (!unar && bo.containsKey(token)) return TokenType.BINAR
    if (unar && uo.containsKey(token)) return TokenType.UNAR
    if (func.containsKey(token)) return TokenType.FUNCTION
    return TokenType.OPERAND
  }  

  private fun tokenize(expr: String): List<Token> {
    val result = mutableListOf<Token>()
    val delimiters = setOf<String>("(", ")", ",") + bo.keys + uo.keys
    var token = ""
    var unar = true
    var needCloseUnar = false;
    for (c in expr + " ") {
      if (token.length > 0) {
        for (delim in delimiters) {
          if (token.endsWith(delim)) {
            token = token.substring(0, token.length - delim.length)
            if (token.length > 0) {
              unar = false
              result.add(Token(type(token), token))
            }
            val t = Token(type(delim, unar), delim)
            if (
              needCloseUnar &&
              (t.type == TokenType.BINAR && bo[t.value]!!.precedence == 1 ||
                t.type == TokenType.RIGHT_PARENTHESIS)
            ) {
              result.add(Token(TokenType.RIGHT_PARENTHESIS))
              needCloseUnar = false
            }
            result.add(t)
            if (unar) {
              result.add(Token(TokenType.LEFT_PARENTHESIS))
              needCloseUnar = true
            }
            if (delim == "(")
              unar = true
            token = ""
            break
          }
        }
      }
      if (!c.isWhitespace()) {
        token += c
      }
    }
    if (token.length > 0) {
      result.add(Token(type(token), token))
    }

    if (needCloseUnar) {
      result.add(Token(TokenType.RIGHT_PARENTHESIS))
    }
    return result
  }

  private fun toPostFix(tokens: List<Token>): List<Token> {
    val output = mutableListOf<Token>()
    val stack  = Stack<Token>()
    for (token in tokens) {
      when (token.type) {
        TokenType.UNAR,
        TokenType.FUNCTION,                     
        TokenType.LEFT_PARENTHESIS -> stack.push(token)
        TokenType.OPERAND -> output.add(token)
        TokenType.BINAR -> {
          while (!stack.isEmpty()) {
            if ((stack.peek().type == TokenType.BINAR)
                && (bo[stack.peek().value]!!.precedence  > bo[token.value]!!.precedence
                ||
                (bo[stack.peek().value]!!.precedence == bo[token.value]!!.precedence
                  && bo[token.value]!!.associate != Assoc.RIGHT)))
                output.add(stack.pop())
            else break
          }
          stack.push(token)
        }
        TokenType.RIGHT_PARENTHESIS -> {
          while (!stack.isEmpty() && stack.peek().type != TokenType.LEFT_PARENTHESIS){
            output.add(stack.pop())
          }
          if (stack.isEmpty() || stack.peek().type != TokenType.LEFT_PARENTHESIS) {
            throw Exception("parentheses mismatched")
          } else {
            stack.pop()
          }
          if (!stack.isEmpty() &&
            (stack.peek().type == TokenType.FUNCTION || stack.peek().type == TokenType.UNAR)
          ){
            output.add(stack.pop())
          }
        }
        TokenType.ARG_SEPARATOR -> {}
      }
    }
    while (!stack.isEmpty()) {
      if (stack.peek().type == TokenType.RIGHT_PARENTHESIS && stack.peek().type == TokenType.LEFT_PARENTHESIS)
          throw Exception("parentheses mismatched")
        output.add(stack.pop())
    }

    return output
  }

  private fun evaluate(tokens: List<Token>): (x: Double) -> Double {
    return fun(x: Double): Double {
      val stack  = Stack<Double>()
      var varName = ""
      for (token in tokens) {
        when (token.type) {
          TokenType.OPERAND -> {
            var v = token.value.toDoubleOrNull()
            if (v == null) {
              if (varName.length > 0 && token.value != varName) {
                throw Exception("Multiple variables are not allowed [$varName, ${token.value}]")
              }
              varName = token.value
              v = x
            }
            stack.push(v)
          }
          TokenType.UNAR -> stack.push(uo[token.value]!!.invoke(stack.pop()))
          TokenType.BINAR -> {
            var op2 = stack.pop()
            var op1 = stack.pop()
            stack.push(bo[token.value]!!.logic.invoke(op1,op2))
          }
          TokenType.FUNCTION -> {
            val args = mutableListOf<Double>()
            for (i in func[token.value]!!.args downTo 1 step 1) {
              args.add(stack.pop())
            }
            stack.push(func[token.value]!!.logic.invoke(args.toDoubleArray()))
          }
          else -> {}
        }
      }
      return stack.pop()
    }
  }

  override fun parse(expression: String): (x: Double) -> Double {
    return evaluate(toPostFix(tokenize(expression)))
  }
}