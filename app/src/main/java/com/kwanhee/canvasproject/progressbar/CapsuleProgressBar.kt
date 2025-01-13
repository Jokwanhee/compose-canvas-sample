package com.kwanhee.canvasproject.progressbar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


/**
 * @param percentage : 채우는 게이지 퍼센트
 * @param offset : 시작 x,y 좌표
 * @param clipOp : ClipOp 속성 (Intersect, Difference)
 * @param cornerRadius : 꼭짓점 radius
 * @param behindBrush : 배경색
 * @param forwardBrush : 채우는 게이지 배경색
 */
@Composable
fun CapsuleProgressBar(
    modifier: Modifier = Modifier,
    percentage: Float = 0f,
    offset: Offset = Offset(0f, 0f),
    clipOp: ClipOp = ClipOp.Intersect,
    cornerRadius: Float = 32f,
    behindBrush: Brush = SolidColor(Color.LightGray),
    forwardBrush: Brush = SolidColor(Color.Green),
) {
    Canvas(
        modifier = modifier
    ) {
        drawCapsule(
            percentage = percentage,
            offset = offset,
            clipOp = clipOp,
            cornerRadius = CornerRadius(cornerRadius.dp.toPx()),
            behindBrush = behindBrush,
            forwardBrush = forwardBrush
        )
    }
}

private fun DrawScope.drawCapsule(
    percentage: Float,
    offset: Offset,
    clipOp: ClipOp,
    cornerRadius: CornerRadius,
    behindBrush: Brush,
    forwardBrush: Brush
) {
    val width = size.width
    val height = size.height

    val behindPath = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(offset = offset, size = Size(width, height)),
                topLeft = cornerRadius,
                topRight = cornerRadius,
                bottomLeft = cornerRadius,
                bottomRight = cornerRadius,
            )
        )
    }

    val forwardPath = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(
                    offset = offset,
                    size = Size(width * percentage, height)
                ),
                topRight = cornerRadius,
                bottomRight = cornerRadius,
            )
        )
    }

    clipPath(
        path = behindPath,
        clipOp = clipOp
    ) {
        drawPath(
            path = behindPath,
            brush = behindBrush
        )
        drawPath(
            path = forwardPath,
            brush = forwardBrush
        )
    }
}

@Preview
@Composable
private fun CapsuleProgressBarPreview() {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    Column {
        CapsuleProgressBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            percentage = sliderPosition,

        )
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it }
        )
    }
}