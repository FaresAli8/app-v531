package com.procalc.app

import java.util.Stack
import kotlin.math.pow
import kotlin.math.sqrt

object MathEvaluator {

    fun evaluate(expression: String): Double {
        val tokens = tokenize(expression)
        val rpn = shuntingYard(tokens)
        return calculateRPN(rpn)
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                    continue
                }
                c == 's' && i + 3 < expr.length && expr.substring(i, i + 4) == "sqrt" -> {
                    tokens.add("sqrt")
                    i += 4
                    continue
                }
                c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '%' || c == '(' || c == ')' -> {
                    tokens.add(c.toString())
                }
            }
            i++
        }
        return tokens
    }

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = Stack<String>()

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token == "sqrt" -> stack.push(token)
                token == "(" -> stack.push(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") {
                        output.add(stack.pop())
                    }
                    if (stack.isNotEmpty()) stack.pop() // Remove '('
                    if (stack.isNotEmpty() && stack.peek() == "sqrt") output.add(stack.pop())
                }
                isOperator(token) -> {
                    while (stack.isNotEmpty() && isOperator(stack.peek()) &&
                        precedence(stack.peek()) >= precedence(token)
                    ) {
                        output.add(stack.pop())
                    }
                    stack.push(token)
                }
            }
        }
        while (stack.isNotEmpty()) {
            output.add(stack.pop())
        }
        return output
    }

    private fun calculateRPN(rpn: List<String>): Double {
        val stack = Stack<Double>()
        for (token in rpn) {
            if (token.toDoubleOrNull() != null) {
                stack.push(token.toDouble())
            } else {
                if (token == "sqrt") {
                    if (stack.isEmpty()) throw IllegalArgumentException("Invalid")
                    val a = stack.pop()
                    stack.push(sqrt(a))
                } else {
                    if (stack.size < 2) throw IllegalArgumentException("Invalid")
                    val b = stack.pop()
                    val a = stack.pop()
                    when (token) {
                        "+" -> stack.push(a + b)
                        "-" -> stack.push(a - b)
                        "*" -> stack.push(a * b)
                        "/" -> stack.push(a / b)
                        "^" -> stack.push(a.pow(b))
                        "%" -> stack.push(a * (b / 100.0))
                    }
                }
            }
        }
        return if (stack.isNotEmpty()) stack.pop() else 0.0
    }

    private fun isOperator(token: String) = token in listOf("+", "-", "*", "/", "^", "%")

    private fun precedence(op: String): Int {
        return when (op) {
            "+", "-" -> 1
            "*", "/", "%" -> 2
            "^" -> 3
            else -> 0
        }
    }
}