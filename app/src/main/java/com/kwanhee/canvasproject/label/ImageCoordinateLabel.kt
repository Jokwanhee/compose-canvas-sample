package com.kwanhee.canvasproject.label

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kwanhee.canvasproject.R

@Immutable
data class LabelWithRect(
    val label: CoordinateLabel,
    val rect: Rect
)

data class CoordinateLabel(
    val name: String,
    val x: Float,
    val y: Float,
)

/**
 * @param label : 이미지 위에 라벨 정보
 * @param horizontalPadding : 이미지와 텍스트 수평 여백 값
 * @param verticalPadding : 이미지와 텍스트 수직 여백 값
 * @param labelTextStyle : 라벨 텍스트 스타일
 * @param labelTextOverFlow : 라벨 텍스트 오버플로우 속성
 * @param labelTextMaxLines : 라벨 텍스트 최대 글자 라인
 * @param onAddRect : 라벨 사각형 추가 람다
 * @param onTapOffset : 클릭 시, 좌표 전달 람다
 */
@Composable
fun ImageCoordinateLabel(
    label: CoordinateLabel,
    modifier: Modifier = Modifier,
    horizontalPadding: Float = 10f,
    verticalPadding: Float = 10f,
    labelTextStyle: TextStyle = TextStyle(
        fontSize = 14.sp,
        color = Color(0xFF121212),
    ),
    labelTextOverFlow: TextOverflow = TextOverflow.Ellipsis,
    labelTextMaxLines: Int = 1,
    onAddRect: (LabelWithRect) -> Unit = {},
    onTapOffset: (Offset) -> Unit = {}
) {
    val textMeasurer = rememberTextMeasurer()

    val measuredText = remember(Unit) {
        textMeasurer.measure(
            text = label.name,
            style = labelTextStyle
        )
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        onTapOffset(tapOffset)
                    }
                )
            }
    ) {
        val x = size.width * label.x
        val y = size.height * label.y

        onAddRect(
            LabelWithRect(
                label = label,
                rect = Rect(
                    topLeft = Offset(x, y),
                    bottomRight = Offset(
                        x + measuredText.size.width.toFloat() + (horizontalPadding * 2),
                        y + measuredText.size.height.toFloat() + (verticalPadding * 2)
                    ),
                )
            )
        )
        drawLabelRect(
            x = x,
            y = y,
            textWidth = measuredText.size.width.toFloat(),
            textHeight = measuredText.size.height.toFloat(),
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding
        )
        drawText(
            textMeasurer = textMeasurer,
            text = label.name,
            topLeft = Offset(x = x + horizontalPadding, y = y + verticalPadding),
            style = labelTextStyle,
            overflow = labelTextOverFlow,
            maxLines = labelTextMaxLines,
        )
    }
}


/**
 * @param x : x 좌표
 * @param y : y 좌표
 * @param textWidth : 텍스트 가로 길이
 * @param textHeight : 텍스트 세로 길이
 * @param horizontalPadding : 컨테이너 도형 내부 수평 패딩 값 (||)
 * @param verticalPadding : 컨테이너 도형 내부 수직 패딩 값 (=)
 * @param roundRectCornerRadius : 컨테이너 도형 corner radius
 * @param roundRectBrush : 컨테이너 도형 색상
 * @param roundRectStroke : 컨테이너 도형 외각선 스타일
 * @param roundRectStrokeBrush : 컨테이너 도형 외각선 색상
 */
private fun DrawScope.drawLabelRect(
    x: Float,
    y: Float,
    textWidth: Float,
    textHeight: Float,
    horizontalPadding: Float,
    verticalPadding: Float,
    roundRectCornerRadius: CornerRadius = CornerRadius(4.dp.toPx()),
    roundRectBrush: Brush = SolidColor(Color(0xFFE81A81)),
    roundRectStroke: Stroke = Stroke(2f),
    roundRectStrokeBrush: Brush = SolidColor(Color.White),
) {
    drawRoundRect(
        topLeft = Offset(x, y),
        cornerRadius = roundRectCornerRadius,
        brush = roundRectBrush,
        size = Size(
            textWidth + (horizontalPadding * 2),
            textHeight + (verticalPadding * 2)
        ),
    )

    drawRoundRect(
        topLeft = Offset(x, y),
        cornerRadius = roundRectCornerRadius,
        brush = roundRectStrokeBrush,
        size = Size(
            textWidth + (horizontalPadding * 2),
            textHeight + (verticalPadding * 2)
        ),
        style = roundRectStroke,
    )
}


val labelBibimbap = CoordinateLabel("비빔밥", 0.5f, 0.5f)
val labelSource = CoordinateLabel("고추장", 0.05f, 0.5f)
val labels = listOf(labelBibimbap, labelSource)

@Preview(showBackground = true)
@Composable
private fun ImageCoordinateLabelPreview() {
    val context = LocalContext.current
    val rects: SnapshotStateList<LabelWithRect> = remember { mutableStateListOf() }

    Box(
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .height(IntrinsicSize.Min)
    ) {
        Image(
            painter = painterResource(R.drawable.bibimbap),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth()
        )
        labels.forEach { label ->
            ImageCoordinateLabel(
                label = label,
                modifier = Modifier
                    .matchParentSize(),
                onAddRect = { rects.add(it) },
                onTapOffset = { offset ->
                    for (i in rects.indices) {
                        if (rects[i].rect.contains(offset)) {
                            Toast.makeText(context, "${rects[i].label}", Toast.LENGTH_SHORT).show()
                            break
                        }
                    }
                }
            )
        }
    }
}