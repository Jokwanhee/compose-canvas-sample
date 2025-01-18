package com.kwanhee.canvasproject.chart

import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// 차트에 보여 줄 데이터 클래스
data class SafeZoneLinData(
    val label: String,
    val value: Float,
    val goalValue: Float,
    val enabled: Boolean = true,
)

// 클릭할 수 있는 원 데이터
@Immutable
data class ClickableDot(
    val value: String,
    val rect: Rect,
    val offset: Offset
)

// 클릭 시, 좌표에 그리기 위한 데이터 클래스
@Immutable
data class DotOffset(
    val offset: Offset,
    val value: String
)

/**
 * SafeZoneLineChart : 안전 영역 라인 차트를 그리는 메인 Composable 함수
 *
 * @param datas : 차트에 표시할 데이터 리스트
 * @param modifier : 차트에 적용할 Modifier
 * @param spacingValue : 차트와 라벨 사이의 간격 값
 * @param enabledGesture : 차트에서 터치 제스처를 사용할지 여부
 * @param visibleXCount : X축에 표시할 라벨의 개수
 * @param visibleYCount : Y축에 표시할 라벨의 개수
 * @param verticalPaddingRect : 클릭 시 생성되는 사각형의 세로 패딩 값
 * @param horizontalPaddingRect : 클릭 시 생성되는 사각형의 가로 패딩 값
 * @param upperGoalRatio : 목표치 상한선 비율
 * @param rectTextOverFlow : 클릭 시 생성되는 사각형 내부 텍스트의 오버플로우 처리 방식
 * @param rectTextMaxLines : 클릭 시 생성되는 사각형 내부 텍스트의 최대 줄 수
 */
@Composable
fun SafeZoneLineChart(
    datas: List<SafeZoneLinData>,
    modifier: Modifier = Modifier,
    spacingValue: Float = 10f,
    enabledGesture: Boolean = false,
    visibleXCount: Int = 5,
    visibleYCount: Int = 5,
    verticalPaddingRect: Float = 5f,
    horizontalPaddingRect: Float = 5f,
    upperGoalRatio: Float = 0.05f,
    rectTextOverFlow: TextOverflow = TextOverflow.Ellipsis,
    rectTextMaxLines: Int = 1,
) {
    val density = LocalDensity.current
    val textXLabelMeasurer = rememberTextMeasurer()
    val textYLabelMeasurer = rememberTextMeasurer()
    val textRectMeasurer = rememberTextMeasurer()

    val rects: SnapshotStateList<ClickableDot> = remember { mutableStateListOf() }
    var clickedDotOffset: DotOffset? by remember { mutableStateOf(null) }

    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000), // 애니메이션 지속 시간 설정
        label = ""
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    LaunchedEffect(clickedDotOffset) {
        if (clickedDotOffset != null) {
            delay(2000)
            clickedDotOffset = null
        }
    }

    val xLabels = remember(datas) {
        datas.takeLast(visibleXCount).map { it.label }
    }
    val yLabels = remember(datas) {
        datas.takeLast(visibleYCount)
            .flatMap {
                listOf(
                    it.value,
                    it.goalValue,
                    (it.goalValue + it.goalValue * upperGoalRatio)
                )
            }
            .yLabelValues(
                if (datas.size < visibleYCount) datas.size else visibleYCount
            )
    }

    val xLabelTextPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.BLACK
            textAlign = Paint.Align.CENTER
            textSize = density.run { 12.sp.toPx() }
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    val yLabelTextPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.BLACK
            textAlign = Paint.Align.LEFT
            textSize = density.run { 12.sp.toPx() }
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    val xLabelTextStyle = TextStyle(
        fontSize = 12.sp
    )

    val yLabelTextStyle = TextStyle(
        fontSize = 12.sp
    )

    val rectTextStyle = TextStyle(
        fontSize = 16.sp,
        color = Color.White
    )

    val textXLabelResult = remember(Unit) {
        textXLabelMeasurer.measure(
            text = "99월99일",
            style = xLabelTextStyle,
        )
    }

    val textYLabelResult = remember(Unit) {
        textYLabelMeasurer.measure(
            text = "999.9",
            style = yLabelTextStyle
        )
    }

    val textRectResult = remember(Unit) {
        textRectMeasurer.measure(
            text = "999.9",
            style = rectTextStyle,
            overflow = rectTextOverFlow,
            maxLines = rectTextMaxLines
        )
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                if (enabledGesture) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            clickedDotOffset = null
                            for (i in rects.indices) {
                                if (rects[i].rect.contains(tapOffset)) {
                                    Log.e("LOG", "value : ${rects[i].value}")
                                    clickedDotOffset = DotOffset(rects[i].offset, rects[i].value)
                                    break
                                }
                            }
                        }
                    )
                }
            }
    ) {
        val spacing = spacingValue.dp.toPx()
        val textXLabelHeight = textXLabelResult.size.height
        val textYLabelWidth = textYLabelResult.size.width

        val outLineWidth = size.width - spacing - textYLabelWidth
        val outLineHeight = size.height - spacing - textXLabelHeight

        drawInnerLine(
            xLabelVisibleCount = visibleXCount,
            yLabelVisibleCount = visibleYCount,
            textYLabelWidth = textYLabelWidth,
            spacing = spacing,
            outLineHeight = outLineHeight,
            outLineWidth = outLineWidth
        )

        drawLabelText(
            xLabels = xLabels,
            yLabels = yLabels,
            outLineWidth = outLineWidth,
            outLineHeight = outLineHeight,
            textYLabelWidth = textYLabelWidth,
            spacing = spacing,
            xLabelTextPaint = xLabelTextPaint,
            yLabelTextPaint = yLabelTextPaint
        )

        drawSafeZone(
            animatedProgress = animatedProgress,
            xLabels = xLabels,
            yLabels = yLabels,
            datas = datas,
            xLabelVisibleCount = visibleXCount,
            outLineWidth = outLineWidth.toInt(),
            outLineHeight = outLineHeight.toInt(),
            textYLabelWidth = textYLabelWidth,
            spacing = spacing
        )


        drawLine(
            animatedProgress = animatedProgress,
            xLabels = xLabels,
            yLabels = yLabels,
            datas = datas,
            xLabelVisibleCount = visibleXCount,
            outLineWidth = outLineWidth.toInt(),
            outLineHeight = outLineHeight.toInt(),
            textYLabelWidth = textYLabelWidth,
            spacing = spacing
        )

        drawOutLine(
            textYLabelWidth = textYLabelWidth,
            spacing = spacing,
            outLineHeight = outLineHeight
        )

        drawDot(
            goalRatio = upperGoalRatio,
            animatedProgress = animatedProgress,
            xLabels = xLabels,
            yLabels = yLabels,
            datas = datas,
            xLabelVisibleCount = visibleXCount,
            outLineWidth = outLineWidth.toInt(),
            outLineHeight = outLineHeight.toInt(),
            textYLabelWidth = textYLabelWidth,
            spacing = spacing,
            onDrawOffset = { offset, radius, index ->
                rects.add(
                    ClickableDot(
                        datas.takeLast(visibleXCount)[index].value.toString(),
                        Rect(
                            topLeft = Offset(offset.x - (radius * 2), offset.y - (radius * 2)),
                            bottomRight = Offset(offset.x + (radius * 2), offset.y + (radius * 2))
                        ),
                        offset = offset
                    )
                )
            }
        )


        // 원 클릭 시, 오른쪽 상단에 Rect 생성
        clickedDotOffset?.let { (offset, value) ->
            drawRoundRect(
                topLeft = offset.copy(
                    x = offset.x,
                    y = offset.y - (verticalPaddingRect.dp.toPx() * 2 + textRectResult.size.height)
                ),
                color = Color.Black.copy(
                    alpha = (.5f)
                ),
                size = Size(
                    (horizontalPaddingRect.dp.toPx() * 2 + textRectResult.size.width),
                    (verticalPaddingRect.dp.toPx() * 2 + textRectResult.size.height)
                ),
                cornerRadius = CornerRadius(4.dp.toPx()),
            )
            drawRoundRect(
                topLeft = offset.copy(
                    x = offset.x,
                    y = offset.y - (verticalPaddingRect.dp.toPx() * 2 + textRectResult.size.height)
                ),
                brush = SolidColor(Color(0xFFE81A81)),
                size = Size(
                    (horizontalPaddingRect.dp.toPx() * 2 + textRectResult.size.width),
                    (verticalPaddingRect.dp.toPx() * 2 + textRectResult.size.height)
                ),
                cornerRadius = CornerRadius(4.dp.toPx()),
                style = Stroke(2.dp.toPx())
            )
            drawText(
                textMeasurer = textRectMeasurer,
                text = value,
                topLeft = Offset(
                    x = offset.x + horizontalPaddingRect.dp.toPx() + 2.dp.toPx(),
                    y = offset.y - (verticalPaddingRect.dp.toPx() + textRectResult.size.height)
                ),
                style = rectTextStyle,
                overflow = rectTextOverFlow,
                maxLines = rectTextMaxLines,
            )
        }
    }
}

/**
 * drawInnerLine : 차트 내부에 수평선과 수직선을 그리는 함수
 *
 * @param xLabelVisibleCount : X축에 표시할 라벨의 개수
 * @param yLabelVisibleCount : Y축에 표시할 라벨의 개수
 * @param textYLabelWidth : Y축 라벨의 너비
 * @param spacing : 차트와 라벨 간 사이 간격
 * @param outLineHeight : 차트의 높이
 * @param outLineWidth : 차트의 너비
 */
private fun DrawScope.drawInnerLine(
    xLabelVisibleCount: Int,
    yLabelVisibleCount: Int,
    textYLabelWidth: Int,
    spacing: Float,
    outLineHeight: Float,
    outLineWidth: Float
) {
    // x선
    for (i in 1..<xLabelVisibleCount) {
        val verticalInnerLineStartX = textYLabelWidth + spacing
        val verticalInnerLineStartY =
            outLineHeight * ((xLabelVisibleCount - i) / xLabelVisibleCount.toFloat())
        val verticalInnerLineEndX = size.width
        val verticalInnerLineEndY =
            outLineHeight * ((xLabelVisibleCount - i) / xLabelVisibleCount.toFloat())

        drawLine(
            color = Color.Black,
            start = Offset(verticalInnerLineStartX, verticalInnerLineStartY),
            end = Offset(verticalInnerLineEndX, verticalInnerLineEndY),
            strokeWidth = (0.5f).dp.toPx(),
            pathEffect = if (i == 0) null else PathEffect.dashPathEffect(
                floatArrayOf(10f, 10f),
                0f
            ),
        )
    }

    // y선
    for (i in 1..<yLabelVisibleCount) {
        val horizontalInnerLineStartX =
            textYLabelWidth + spacing + (outLineWidth * (i / yLabelVisibleCount.toFloat()))
        val horizontalInnerLineStartY = outLineHeight
        val horizontalInnerLineEndX =
            textYLabelWidth + spacing + (outLineWidth * (i / yLabelVisibleCount.toFloat()))
        val horizontalInnerLineEndY = 0f

        drawLine(
            color = Color.Black,
            start = Offset(horizontalInnerLineStartX, horizontalInnerLineStartY),
            end = Offset(horizontalInnerLineEndX, horizontalInnerLineEndY),
            strokeWidth = (0.5f).dp.toPx(),
            pathEffect = if (i == 0) null else PathEffect.dashPathEffect(
                floatArrayOf(10f, 10f),
                0f
            ),
        )
    }
}


/**
 * drawOutLine : 차트의 외곽선을 그리는 함수
 *
 * @param textYLabelWidth : Y축 라벨의 너비
 * @param spacing : 차트와 라벨 간 사이 간격
 * @param outLineHeight : 차트의 높이
 */
private fun DrawScope.drawOutLine(
    textYLabelWidth: Int,
    spacing: Float,
    outLineHeight: Float
) {
    // x 선
    drawLine(
        color = Color.Black,
        start = Offset(textYLabelWidth + spacing, outLineHeight),
        end = Offset(size.width, outLineHeight),
        strokeWidth = 1.dp.toPx()
    )

    // y 선
    drawLine(
        color = Color.Black,
        start = Offset(textYLabelWidth + spacing, outLineHeight),
        end = Offset(textYLabelWidth + spacing, 0f),
        strokeWidth = 1.dp.toPx()
    )
}

/**
 * drawLabelText : X축과 Y축에 라벨을 그리는 함수
 *
 * @param xLabels : X축에 표시할 라벨 리스트
 * @param yLabels : Y축에 표시할 라벨 리스트
 * @param outLineWidth : 차트의 너비
 * @param outLineHeight : 차트의 높이
 * @param textYLabelWidth : Y축 라벨의 너비
 * @param spacing : 차트와 라벨 간 사이 간격
 * @param xLabelTextPaint : X축 라벨의 텍스트 스타일을 설정하는 Paint 객체
 * @param yLabelTextPaint : Y축 라벨의 텍스트 스타일을 설정하는 Paint 객체
 */
private fun DrawScope.drawLabelText(
    xLabels: List<String>,
    yLabels: List<Float>,
    outLineWidth: Float,
    outLineHeight: Float,
    textYLabelWidth: Int,
    spacing: Float,
    xLabelTextPaint: Paint,
    yLabelTextPaint: Paint
) {
    xLabels.forEachIndexed { i, label ->
        val xLabelSpace = (i / xLabels.size.toFloat()) * outLineWidth
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                label,
                (textYLabelWidth + spacing) + xLabelSpace,
                size.height,
                xLabelTextPaint
            )
        }
    }

    yLabels.forEachIndexed { i, label ->
        val yLabelSpace = ((yLabels.size.toFloat() - i) / yLabels.size.toFloat()) * outLineHeight
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                label.toString(),
                0f,
                yLabelSpace,
                yLabelTextPaint
            )
        }
    }
}


/**
 * drawSafeZone : 안전 영역을 그리는 함수
 *
 * @param animatedProgress : 애니메이션 진행률
 * @param xLabels : X축에 표시할 라벨 리스트
 * @param yLabels : Y축에 표시할 라벨 리스트
 * @param datas : 차트에 표시할 데이터 리스트
 * @param xLabelVisibleCount : X축에 표시할 라벨의 개수
 * @param outLineWidth : 차트의 너비
 * @param outLineHeight : 차트의 높이
 * @param textYLabelWidth : Y축 라벨의 너비
 * @param spacing : 차트와 라벨 간 사이 간격
 */
private fun DrawScope.drawSafeZone(
    animatedProgress: Float,
    xLabels: List<String>,
    yLabels: List<Float>,
    datas: List<SafeZoneLinData>,
    xLabelVisibleCount: Int,
    outLineWidth: Int,
    outLineHeight: Int,
    textYLabelWidth: Int,
    spacing: Float
) {
    var startX: Float = 0f
    var startY: Float = 0f
    var endX: Float = 0f
    var endY: Float = 0f
    val goalMinOffset = mutableListOf<Offset>()
    val goalMaxOffset = mutableListOf<Offset>()

    val goalMinPath = Path().apply {
        if (xLabels.isEmpty() || yLabels.isEmpty()) return@apply
        datas.takeLast(xLabelVisibleCount).forEachIndexed { i, safeZoneLinData ->
            val goal = safeZoneLinData.goalValue
            val spacingX = (i / xLabels.size.toFloat()) * outLineWidth
            val yRatio =
                (goal - yLabels.first()) / (yLabels.last() - yLabels.first())

            val x = textYLabelWidth + spacing + spacingX
            val y =
                outLineHeight - ((yLabels.size - 1) / yLabels.size.toFloat()) * outLineHeight +
                        (outLineHeight - (1 / yLabels.size.toFloat()) * outLineHeight) * (1f - yRatio)

            goalMinOffset.add(Offset(x, y))
            if (i == 0) {
                startX = x
                startY = y
                moveTo(x, y)
            }
            lineTo(x, y)
            if (i == xLabelVisibleCount - 1) {
                lineTo(textYLabelWidth + spacing + outLineWidth, y)
                goalMinOffset.add(Offset(textYLabelWidth + spacing + outLineWidth, y))
            }
        }
    }

    drawPath(
        path = goalMinPath,
        brush = SolidColor(
            Color(0xFF00B660).copy(
                alpha = animatedProgress
            ),
        ),
        style = Stroke(
            width = 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
    )

    val goalMaxPath = Path().apply {
        if (xLabels.isEmpty() || yLabels.isEmpty()) return@apply
        datas.takeLast(xLabelVisibleCount).forEachIndexed { i, safeZoneLinData ->
            val goal = (safeZoneLinData.goalValue + safeZoneLinData.goalValue * 0.05f)
            val spacingX = (i / xLabels.size.toFloat()) * outLineWidth
            val yRatio = (goal - yLabels.first()) / (yLabels.last() - yLabels.first())

            val x = textYLabelWidth + spacing + spacingX
            val y =
                outLineHeight - ((yLabels.size - 1) / yLabels.size.toFloat()) * outLineHeight +
                        (outLineHeight - (1 / yLabels.size.toFloat()) * outLineHeight) * (1f - yRatio)

            goalMaxOffset.add(Offset(x, y))
            if (i == 0) {
                moveTo(x, y)
            }
            lineTo(x, y)
            if (i == xLabelVisibleCount - 1) {
                lineTo(textYLabelWidth + spacing + outLineWidth, y)
                endX = textYLabelWidth + spacing + outLineWidth
                endY = y
                goalMaxOffset.add(Offset(textYLabelWidth + spacing + outLineWidth, y))
            }
        }
    }

    drawPath(
        path = goalMaxPath,
        brush = SolidColor(
            Color(0xFF00B660).copy(
                alpha = animatedProgress
            ),
        ),
        style = Stroke(
            width = 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
    )

    val safeZonePath = Path().apply {
        // 최소 목표치 라인의 시작점으로 이동
        moveTo(startX, startY)

        // 최소 목표치 offset
        goalMinOffset.forEach {
            lineTo(it.x, it.y)
        }

        // 최대 목표치 라인의 마지막점으로 이동
        lineTo(endX, endY)

        // 최대 목표치 offset(역순)
        goalMaxOffset.reversed().forEach {
            lineTo(it.x, it.y)
        }

        close()
    }

    drawPath(
        path = safeZonePath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Green.copy(alpha = (.4f) * animatedProgress),
                Color.Transparent
            ),
        ),
        style = Fill
    )
}

/**
 * drawLine : 기본 라인을 그리는 함수
 *
 * @param animatedProgress : 애니메이션 진행률
 * @param xLabels : X축에 표시할 라벨 리스트
 * @param yLabels : Y축에 표시할 라벨 리스트
 * @param datas : 차트에 표시할 데이터 리스트
 * @param xLabelVisibleCount : X축에 표시할 라벨의 개수
 * @param outLineWidth : 차트의 너비
 * @param outLineHeight : 차트의 높이
 * @param textYLabelWidth : Y축 라벨의 너비
 * @param spacing : 차트와 라벨 간 사이 간격
 */
private fun DrawScope.drawLine(
    animatedProgress: Float,
    xLabels: List<String>,
    yLabels: List<Float>,
    datas: List<SafeZoneLinData>,
    xLabelVisibleCount: Int,
    outLineWidth: Int,
    outLineHeight: Int,
    textYLabelWidth: Int,
    spacing: Float
) {
    if (xLabels.isEmpty() || yLabels.isEmpty()) return

    val linPath = Path().apply {
        datas.takeLast(xLabelVisibleCount).forEachIndexed { i, safeZoneLinData ->
            val value = safeZoneLinData.value
            val spacingX = (i / xLabels.size.toFloat()) * outLineWidth
            val yRatio =
                (value - yLabels.first()) / (yLabels.last() - yLabels.first())

            val x = textYLabelWidth + spacing + spacingX
            val y =
                outLineHeight - ((yLabels.size - 1) / yLabels.size.toFloat()) * outLineHeight +
                        (outLineHeight - (1 / yLabels.size.toFloat()) * outLineHeight) * (1f - yRatio)

            if (i == 0) {
                moveTo(x, y)
            }
            lineTo(x, y)
            if (i == xLabelVisibleCount - 1) {
                lineTo(textYLabelWidth + spacing + outLineWidth, y)
            }
        }
    }

    drawPath(
        path = linPath,
        brush = SolidColor(
            Color(0xFF000000).copy(
                alpha = animatedProgress
            ),
        ),
        style = Stroke(
            width = 1.dp.toPx(),
        )
    )
}

/**
 * drawDot : 원을 그리는 함수
 *
 * @param animatedProgress : 애니메이션 진행률
 * @param xLabels : X축에 표시할 라벨 리스트
 * @param yLabels : Y축에 표시할 라벨 리스트
 * @param datas : 차트에 표시할 데이터 리스트
 * @param xLabelVisibleCount : X축에 표시할 라벨의 개수
 * @param outLineWidth : 차트의 너비
 * @param outLineHeight : 차트의 높이
 * @param textYLabelWidth : Y축 라벨의 너비
 * @param spacing : 차트와 라벨 간 사이 간격
 * @param onDrawCircle : 원을 그릴 때 호출되는 콜백 함수
 */
private fun DrawScope.drawDot(
    goalRatio: Float,
    animatedProgress: Float,
    xLabels: List<String>,
    yLabels: List<Float>,
    datas: List<SafeZoneLinData>,
    xLabelVisibleCount: Int,
    outLineWidth: Int,
    outLineHeight: Int,
    textYLabelWidth: Int,
    spacing: Float,
    onDrawOffset: (Offset, Float, Int) -> Unit,
) {
    if (yLabels.isEmpty()) return

    val radius = 7.dp.toPx()

    datas.takeLast(xLabelVisibleCount).forEachIndexed { i, safeZoneLinData ->
        val value = safeZoneLinData.value
        val spacingX = (i / xLabels.size.toFloat()) * outLineWidth
        val yRatio =
            (value - yLabels.first()) / (yLabels.last() - yLabels.first())

        val x = textYLabelWidth + spacing + spacingX
        val y =
            outLineHeight - ((yLabels.size - 1) / yLabels.size.toFloat()) * outLineHeight +
                    (outLineHeight - (1 / yLabels.size.toFloat()) * outLineHeight) * (1f - yRatio)

        onDrawOffset(Offset(x, y), radius, i)

        // 원 내부
        drawCircle(
            brush = if (safeZoneLinData.enabled) {
                if (safeZoneLinData.value in safeZoneLinData.goalValue..(safeZoneLinData.goalValue + safeZoneLinData.goalValue * goalRatio)) {
                    SolidColor(Color(0xFF00B660).copy(alpha = animatedProgress))
                } else {
                    SolidColor(Color(0xFFFF0000).copy(alpha = animatedProgress))
                }
            } else SolidColor(
                Color.LightGray.copy(alpha = animatedProgress)
            ),
            radius = radius,
            center = Offset(x, y),
        )

        // 원 테두리
        drawCircle(
            brush = SolidColor(
                Color.White.copy(alpha = animatedProgress)
            ),
            radius = radius,
            center = Offset(x, y),
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}


/**
 * yLabelValues : Y축에 표시할 라벨 순열
 *
 * @param count : Y축에 표시할 라벨의 개수
 */
private fun List<Float>.yLabelValues(count: Int): List<Float> {
    if (this.isEmpty()) return emptyList()

    val minValue = this.minOrNull()?.minus(1) ?: return emptyList()
    val maxValue = this.maxOrNull()?.plus(1) ?: return emptyList()

    val interval = if (count == 1) {
        count.toFloat()
    } else {
        (maxValue - minValue) / (count - 1)
    }

    val labelValues = mutableListOf(String.format("%.1f", minValue).toFloat())

    for (i in 1..(count - 2)) {
        val nextValue = minValue + interval * i
        labelValues.add(String.format("%.1f", nextValue).toFloat())
    }

    labelValues.add(String.format("%.1f", maxValue).toFloat())
    return labelValues.sorted()
}

@Preview(showBackground = true)
@Composable
private fun SafeZoneLineChartPreview() {
    val data1 = SafeZoneLinData("12월27일", 52.6f, 51.2f)
    val data2 = SafeZoneLinData("12월27일", 52.6f, 51.3f)
    val data3 = SafeZoneLinData("12월27일", 51.2f, 51.5f)
    val data4 = SafeZoneLinData("12월27일", 51.1f, 51.5f)
    val data5 = SafeZoneLinData("12월27일", 51.3f, 51.5f)
    val datas = listOf(data1, data2, data3, data4, data5)
    SafeZoneLineChart(
        datas = datas,
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxWidth()
            .height(250.dp),
//            .fillMaxHeight()
        enabledGesture = true
    )
}