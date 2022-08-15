package com.example.calculator

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import java.util.*

class MainViewModel : ViewModel() {

    companion object {
        private const val OPERATOR_ADD = "+"
        private const val OPERATOR_SUBTRACT = "-"
        private const val OPERATOR_MULTIPLY = "×"
        private const val OPERATOR_DIVIDE = "÷"
        private const val OPERATOR_SURPLUS = "%"
    }

    private val _result = MutableStateFlow("")
    val result = _result.asStateFlow()
    private val numbers = mutableStateListOf<String>()
    private val operators = mutableStateListOf<String>()
    private val _surplusCount = MutableStateFlow(0)
    private val surplusCount = _surplusCount.asStateFlow()

    fun clear() {
        reset()

        result.value.let {
            if (it.isNotEmpty()) _result.value = ""
        }
    }

    private fun reset() {
        numbers.clear()
        operators.clear()
        _surplusCount.value = 0
    }

    fun operation(char: String) {
        when (char) {
            OPERATOR_ADD, OPERATOR_SUBTRACT, OPERATOR_MULTIPLY, OPERATOR_DIVIDE -> {
                if (numbers.isEmpty()) {
                    numbers.add("0")
                    operators.add(char)
                    _result.value = "0$char"
                } else {
                    if (numbers.size == operators.size) {
                        if (operators.last() != char) {
                            operators.removeLast()
                            operators.add(char)
                            _result.value = result.value.let {
                                "${it.substring(0, it.length - 1)}$char"
                            }
                        }
                    } else {
                        operators.add(char)
                        _result.value = "${result.value}$char"
                    }
                }
            }
            OPERATOR_SURPLUS -> {
                _surplusCount.value = _surplusCount.value + 1
                if (numbers.isEmpty()) {
                    numbers.add("%")
                    _result.value = "%"
                } else {
                    if (numbers.size == operators.size) {
                        numbers.add("%")
                    } else {
                        numbers.add("${numbers.removeLast()}%")
                    }
                    _result.value = "${result.value}%"
                }
            }
            "." -> {
                if (numbers.isEmpty()) {
                    numbers.add("0.")
                    _result.value = "0$char"
                } else {
                    if (numbers.size == operators.size) {
                        numbers.add("0.")
                        _result.value = "${result.value}0."
                    } else {
                        if (!numbers.last().contains(".")) {
                            numbers.removeLast().let {
                                numbers.add("$it.")
                            }
                            _result.value = "${result.value}."
                        }
                    }
                }
            }
            else -> {
                if (numbers.size == operators.size) {
                    numbers.add(char)
                    _result.value = "${result.value}$char"
                } else {
                    if (!numbers.last().contains("%")) {
                        numbers.removeLast().let {
                            numbers.add("$it$char")
                        }
                        _result.value = "${result.value}$char"
                    }
                }
            }
        }
        println(numbers)
        println(operators)
    }

    fun delete() {
        if (numbers.size == operators.size) {
            if (numbers.isNotEmpty()) {
                operators.removeLast()
            }
        } else {
            numbers.removeLast().let {
                if (it.contains("%")) {
                    _surplusCount.value = _surplusCount.value - 1
                }
                if (it.length > 1) {
                    numbers.add(it.substring(0, it.length - 1))
                }
            }
        }

        _result.value = result.value.let {
            if (it.isNotEmpty()) it.substring(0, it.length - 1) else ""
        }
    }

    fun calculate() {
        if (numbers.size == operators.size) {
            return
        }

        while (surplusCount.value > 0) {
            numbers.indexOfFirst {
                it.contains("%")
            }.also {
                val number = numbers[it]
                val sb = StringBuilder()
                var count = 0
                number.forEach { char ->
                    if (char == '%') {
                        count++
                        _surplusCount.value = _surplusCount.value - 1
                    } else {
                        sb.append(char)
                    }
                }
                numbers[it] =
                    sb.toString().toBigDecimal().divide(BigDecimal(100).pow(count)).toString()
            }
        }

        val newNumbers = LinkedList<String>()
        val newOperators = LinkedList<String>()
        while (operators.isNotEmpty()) {
            operators.removeAt(0).also {
                val number1 = numbers.removeAt(0)
                when (it) {
                    OPERATOR_MULTIPLY -> {
                        numbers[0] =
                            BigDecimal(number1).multiply(BigDecimal(numbers[0])).toString()
                        if (operators.isEmpty()) {
                            newNumbers.offer(numbers[0])
                        }
                    }
                    OPERATOR_DIVIDE -> {
                        numbers[0] = BigDecimal(number1).divide(
                            BigDecimal(numbers[0]),
                            4,
                            BigDecimal.ROUND_HALF_UP
                        ).toString()
                        if (operators.isEmpty()) {
                            newNumbers.offer(numbers[0])
                        }
                    }
                    else -> {
                        newNumbers.offer(number1)
                        newOperators.offer(it)
                        if (operators.isEmpty()) {
                            newNumbers.offer(numbers[0])
                        }
                    }
                }
            }
        }

        if (newOperators.isNotEmpty()) {
            var firstNumber = BigDecimal(newNumbers.removeAt(0))
            newOperators.forEach {
                when (it) {
                    OPERATOR_ADD -> {
                        firstNumber = firstNumber.add(BigDecimal(newNumbers.removeAt(0)))
                    }
                    OPERATOR_SUBTRACT -> {
                        firstNumber = firstNumber.subtract(BigDecimal(newNumbers.removeAt(0)))
                    }
                }
            }
            _result.value = firstNumber.toString()
        } else {
            _result.value = newNumbers[0]
        }

        reset()
    }
}