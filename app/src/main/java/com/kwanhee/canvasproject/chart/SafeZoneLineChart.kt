package com.kwanhee.canvasproject.chart

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

data class SafeZoneLinData(
    val label: String,
    val value: Float,
    val enabled: Boolean = true
)

@Composable
fun SafeZoneLineChart(
    datas: List<SafeZoneLinData>,
    spacing: Float = 10f,
    modifier: Modifier = Modifier
//    safeRange
) {
    Canvas(
        modifier = modifier
    ) {


        val spacingX = size.width
    }
}

@Preview
@Composable
private fun SafeZoneLineChartPreview() {
    val data = SafeZoneLinData("1월2일", 1f)
    val datas = List(5) { data }
    SafeZoneLineChart(
        datas = datas
    )
}