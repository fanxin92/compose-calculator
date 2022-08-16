package com.example.calculator

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.calculator.ui.theme.CalculatorTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uiMode = resources.configuration.uiMode
        val isLightTheme =
            (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO
        window.statusBarColor =
            if (isLightTheme) Color(0xFFFAFAFA).toArgb() else Color(0xFF060606).toArgb()
        if (isLightTheme) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            val decorView = window.decorView
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                decorView.windowInsetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility =
                    decorView.systemUiVisibility xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }

        setContent {
            CalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        VerticalLayout(viewModel)
                    } else {
                        HorizontalLayout(viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalUnitApi::class)
@Suppress("StateFlowValueCalledInComposition")
@Composable
fun VerticalLayout(viewModel: MainViewModel) {
    val text = viewModel.result.collectAsState()
    Column(
        Modifier
            .fillMaxSize()
    ) {
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .weight(1.0F)
                .padding(10.dp),
            listState,
            verticalArrangement = Arrangement.Bottom
        ) {
            var count = 0
            val textLayout: (TextLayoutResult) -> Unit = { result ->
                if (result.lineCount != count) {
                    count = result.lineCount
                    coroutineScope.launch {
                        viewModel.result.collectLatest {
                            listState.animateScrollToItem(0, result.size.height)
                        }
                    }
                }
            }
            item {
                Text(
                    text = text.value,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    fontSize = TextUnit(36F, TextUnitType.Sp),
                    onTextLayout = textLayout
                )
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1.0F)
                .background(if (MaterialTheme.colors.isLight) Color.White else Color(0XFF171717))
        ) {
            val normalModifier = Modifier
                .weight(1.0F)
                .fillMaxHeight()
                .padding(10.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1F)
            ) {
                OperatorButton(text = "C", normalModifier) {
                    viewModel.clear()
                }
                OperatorButton(text = "%", normalModifier) {
                    viewModel.operation("%")
                }
                OperatorButton(text = "Del", normalModifier) {
                    viewModel.delete()
                }
                LightOperatorButton(text = "÷", normalModifier) {
                    viewModel.operation("÷")
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1F)
            ) {
                OperatorButton(text = "7", normalModifier) {
                    viewModel.operation("7")
                }
                OperatorButton(text = "8", normalModifier) {
                    viewModel.operation("8")
                }
                OperatorButton(text = "9", normalModifier) {
                    viewModel.operation("9")
                }
                LightOperatorButton(text = "×", normalModifier) {
                    viewModel.operation("×")
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1F)
            ) {
                OperatorButton(text = "4", normalModifier) {
                    viewModel.operation("4")
                }
                OperatorButton(text = "5", normalModifier) {
                    viewModel.operation("5")
                }
                OperatorButton(text = "6", normalModifier) {
                    viewModel.operation("6")
                }
                LightOperatorButton(text = "-", normalModifier) {
                    viewModel.operation("-")
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1F)
            ) {
                OperatorButton(text = "1", normalModifier) {
                    viewModel.operation("1")
                }
                OperatorButton(text = "2", normalModifier) {
                    viewModel.operation("2")
                }
                OperatorButton(text = "3", normalModifier) {
                    viewModel.operation("3")
                }
                LightOperatorButton(text = "+", normalModifier) {
                    viewModel.operation("+")
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1F)
            ) {
                OperatorButton(text = "00", normalModifier) {
                    viewModel.operation("00")
                }
                OperatorButton(text = "0", normalModifier) {
                    viewModel.operation("0")
                }
                OperatorButton(text = ".", normalModifier) {
                    viewModel.operation(".")
                }
                LightOperatorButton(text = "=", normalModifier) {
                    viewModel.calculate()
                }
            }
        }
    }
}

@OptIn(ExperimentalUnitApi::class)
@Suppress("StateFlowValueCalledInComposition")
@Composable
fun HorizontalLayout(viewModel: MainViewModel) {
    val text = viewModel.result.collectAsState()
    Row(
        Modifier
            .fillMaxHeight()
    ) {
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        LazyColumn(
            Modifier
                .fillMaxHeight()
                .weight(1.0F)
                .padding(10.dp),
            listState,
            verticalArrangement = Arrangement.Top
        ) {
            var count = 0
            val textLayout: (TextLayoutResult) -> Unit = { result ->
                if (result.lineCount != count) {
                    count = result.lineCount
                    coroutineScope.launch {
                        viewModel.result.collectLatest {
                            listState.animateScrollToItem(0, result.size.height)
                        }
                    }
                }
            }
            item {
                Text(
                    text = text.value,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    fontSize = TextUnit(36F, TextUnitType.Sp),
                    onTextLayout = textLayout
                )
            }
        }
        Column(
            Modifier
                .fillMaxHeight()
                .weight(1.0F)
                .background(if (MaterialTheme.colors.isLight) Color.White else Color(0XFF171717))
        ) {
            val normalModifier = Modifier
                .weight(1.0F)
                .fillMaxHeight()
                .padding(10.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1F)
            ) {
                OperatorButton(text = "C", normalModifier) {
                    viewModel.clear()
                }
                OperatorButton(text = "%", normalModifier) {
                    viewModel.operation("%")
                }
                OperatorButton(text = "Del", normalModifier) {
                    viewModel.delete()
                }
                LightOperatorButton(text = "÷", normalModifier) {
                    viewModel.operation("÷")
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1F)
            ) {
                OperatorButton(text = "7", normalModifier) {
                    viewModel.operation("7")
                }
                OperatorButton(text = "8", normalModifier) {
                    viewModel.operation("8")
                }
                OperatorButton(text = "9", normalModifier) {
                    viewModel.operation("9")
                }
                LightOperatorButton(text = "×", normalModifier) {
                    viewModel.operation("×")
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1F)
            ) {
                OperatorButton(text = "4", normalModifier) {
                    viewModel.operation("4")
                }
                OperatorButton(text = "5", normalModifier) {
                    viewModel.operation("5")
                }
                OperatorButton(text = "6", normalModifier) {
                    viewModel.operation("6")
                }
                LightOperatorButton(text = "-", normalModifier) {
                    viewModel.operation("-")
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1F)
            ) {
                OperatorButton(text = "1", normalModifier) {
                    viewModel.operation("1")
                }
                OperatorButton(text = "2", normalModifier) {
                    viewModel.operation("2")
                }
                OperatorButton(text = "3", normalModifier) {
                    viewModel.operation("3")
                }
                LightOperatorButton(text = "+", normalModifier) {
                    viewModel.operation("+")
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1F)
            ) {
                OperatorButton(text = "00", normalModifier) {
                    viewModel.operation("00")
                }
                OperatorButton(text = "0", normalModifier) {
                    viewModel.operation("0")
                }
                OperatorButton(text = ".", normalModifier) {
                    viewModel.operation(".")
                }
                LightOperatorButton(text = "=", normalModifier) {
                    viewModel.calculate()
                }
            }
        }
    }
}

@OptIn(ExperimentalUnitApi::class)
@Composable
fun OperatorButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    TextButton(onClick, modifier) {
        Text(text = text, fontSize = TextUnit(22F, TextUnitType.Sp))
    }
}

@OptIn(ExperimentalUnitApi::class)
@Composable
fun LightOperatorButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    val isLightTheme = MaterialTheme.colors.isLight
    TextButton(
        onClick, modifier,
        colors = ButtonDefaults.textButtonColors(
            if (isLightTheme) Color(0xFFF5F5F5) else Color(0xFF404040)
        )
    ) {
        val textColor = if (isLightTheme) Color.Black else Color.White
        Text(text = text, color = textColor, fontSize = TextUnit(22F, TextUnitType.Sp))
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CalculatorTheme {
        VerticalLayout(MainViewModel())
    }
}