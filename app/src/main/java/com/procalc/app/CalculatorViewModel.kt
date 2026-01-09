package com.procalc.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CalculatorState(
    val expression: String = "",
    val result: String = "",
    val history: List<String> = emptyList()
)

class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorState())
    val uiState: StateFlow<CalculatorState> = _uiState.asStateFlow()

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> append(action.number)
            is CalculatorAction.Operator -> append(action.operator)
            is CalculatorAction.Clear -> _uiState.value = _uiState.value.copy(expression = "", result = "")
            is CalculatorAction.Delete -> deleteLast()
            is CalculatorAction.Calculate -> calculate()
            is CalculatorAction.Parenthesis -> append("()") // Simplified logic for demo
            is CalculatorAction.Decimal -> append(".")
            is CalculatorAction.ClearHistory -> _uiState.value = _uiState.value.copy(history = emptyList())
            is CalculatorAction.LoadHistory -> loadHistory(action.expression)
        }
    }

    private fun loadHistory(expr: String) {
        _uiState.value = _uiState.value.copy(expression = expr, result = "")
    }

    private fun append(str: String) {
        _uiState.value = _uiState.value.copy(
            expression = _uiState.value.expression + str
        )
    }

    private fun deleteLast() {
        val current = _uiState.value.expression
        if (current.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(expression = current.dropLast(1))
        }
    }

    private fun calculate() {
        val expr = _uiState.value.expression
        if (expr.isEmpty()) return
        viewModelScope.launch {
            try {
                val resultDouble = MathEvaluator.evaluate(expr)
                // Format: Remove .0 if integer
                val resultStr = if (resultDouble % 1.0 == 0.0) {
                    resultDouble.toLong().toString()
                } else {
                    resultDouble.toString()
                }
                
                val newHistory = _uiState.value.history.toMutableList()
                newHistory.add(0, "$expr = $resultStr")
                
                _uiState.value = _uiState.value.copy(
                    result = resultStr,
                    history = newHistory
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(result = "Error")
            }
        }
    }
}

sealed class CalculatorAction {
    data class Number(val number: String) : CalculatorAction()
    data class Operator(val operator: String) : CalculatorAction()
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    object Calculate : CalculatorAction()
    object Parenthesis : CalculatorAction()
    object Decimal : CalculatorAction()
    object ClearHistory : CalculatorAction()
    data class LoadHistory(val expression: String) : CalculatorAction()
}