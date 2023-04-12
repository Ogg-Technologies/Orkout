package com.oggtechnologies.orkout.ui.views

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oggtechnologies.orkout.ui.theme.OrkoutTheme
import java.lang.Float.min

data class GraphDataPoint(
    val x: Float,
    val y: Float,
    val dotSize: Float = 5f,
    val label: String? = null,
    val xLabel: String? = null,
    val yLabel: String? = null,
)

@Composable
fun Graph(
    points: List<GraphDataPoint>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val minX = points.minByOrNull { it.x }?.x ?: 0f
    val maxX = points.maxByOrNull { it.x }?.x ?: 0f
    val minY = min(points.minByOrNull { it.y }?.y ?: 0f, 0f)
    val maxY = points.maxByOrNull { it.y }?.y ?: 0f
    val xRange = maxX - minX
    val yRange = maxY - minY
    val graphColor = MaterialTheme.colors.secondary

    var selectedPointIndex: Int? by remember { mutableStateOf(null) }

    BoxWithConstraints(
        modifier = modifier
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(12.dp))
    ) {
        val leftPad = LocalDensity.current.run {
            contentPadding.calculateLeftPadding(
                LocalLayoutDirection.current
            ).toPx()
        }
        val topPad = LocalDensity.current.run {
            contentPadding.calculateTopPadding().toPx()
        }
        val width = LocalDensity.current.run { maxWidth.toPx() } - leftPad * 2
        val height = LocalDensity.current.run { maxHeight.toPx() } - topPad * 2

        val animationProgress = enteringAnimation()

        fun getCanvasPoints(lerpYTime: Float): List<GraphDataPoint> {
            return points.map {
                it.copy(
                    x = (it.x - minX) / xRange * width,
                    y = height - (it.y - minY) / yRange * height * lerpYTime,
                )
            }
        }

        val canvasPoints = getCanvasPoints(animationProgress.value)

        val onTap = { tapOffset: Offset ->
            val canvasTapPos = tapOffset - Offset(leftPad, topPad)
            // Needed because onTap is not recreated as animation progresses otherwise
            val updatedCanvasPoints = getCanvasPoints(animationProgress.value)
            val pointIndex = getTappedPointIndex(updatedCanvasPoints, canvasTapPos)
            selectedPointIndex = pointIndex
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = onTap,
                    )
                }
                .padding(contentPadding)
        ) {
            val strokePath = createPathFrom(canvasPoints)
            val fillPath = android.graphics.Path(strokePath.asAndroidPath())
                .asComposePath()
                .apply {
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        graphColor.copy(alpha = 0.3f),
                        graphColor.copy(alpha = 0f),
                    )
                )
            )
            drawPath(
                path = strokePath,
                color = graphColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            )
            selectedPointIndex?.let { index ->
                val point = canvasPoints[index]
                drawRadialAroundPoint(point, graphColor)
                val label = point.label
                if (label != null) {
                    drawLabelForPoint(point, label)
                }
                val xLabel = point.xLabel
                if (xLabel != null) {
                    drawXLabelForPoint(point, xLabel)
                }
            }
            for (point in canvasPoints) {
                drawCircle(
                    color = graphColor,
                    radius = point.dotSize,
                    center = Offset(point.x, point.y)
                )
            }
        }

    }
}

@Composable
private fun enteringAnimation(
    start: Float = 0f,
    end: Float = 1f,
    durationMillis: Int = 500
): State<Float> {
    var progress by remember { mutableStateOf(start) }
    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = durationMillis)
    )
    LaunchedEffect(Unit) {
        progress = end
    }
    return animatedProgress
}

private fun DrawScope.drawXLabelForPoint(
    point: GraphDataPoint,
    xLabel: String
) {
    drawLine(
        color = Color.Gray.copy(alpha = 0.5f),
        start = Offset(point.x, size.height - 20),
        end = Offset(point.x, size.height + 20),
        strokeWidth = 1.dp.toPx(),
    )
    drawIntoCanvas { canvas ->
        val labelTextSize = 40f
        val paint = Paint().apply {
            isAntiAlias = true
            color = White.toArgb()
            textSize = labelTextSize
            textAlign = Paint.Align.CENTER
        }
        canvas.nativeCanvas.drawText(
            xLabel,
            point.x,
            size.height + 30 + labelTextSize,
            paint
        )
    }
}

private fun DrawScope.drawLabelForPoint(
    point: GraphDataPoint,
    label: String
) {
    drawIntoCanvas { canvas ->
        val labelTextSize = 40f
        val paint = Paint().apply {
            isAntiAlias = true
            color = White.toArgb()
            textSize = labelTextSize
            textAlign = Paint.Align.CENTER
        }
        val lines = label.split("\n")
        lines.mapIndexed { index, line ->
            canvas.nativeCanvas.drawText(
                line,
                point.x,
                point.y - (lines.size - index) * labelTextSize,
                paint
            )
        }
    }
}

private fun DrawScope.drawRadialAroundPoint(
    point: GraphDataPoint,
    color: Color
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = 0.4f),
                color.copy(alpha = 0f),
            ),
            center = Offset(point.x, point.y),
            radius = 40 + point.dotSize,
        ),
        center = Offset(point.x, point.y),
    )
}

private fun createPathFrom(canvasPoints: List<GraphDataPoint>): Path =
    Path().apply {
        canvasPoints.zipWithNext().forEachIndexed { index, (point1, point2) ->
            if (index == 0) {
                moveTo(point1.x, point1.y)
            }
            val lerpT = 0.6f
            cubicTo(
                point1.x * (1 - lerpT) + point2.x * lerpT,
                point1.y,
                point1.x * (lerpT) + point2.x * (1 - lerpT),
                point2.y,
                point2.x,
                point2.y
            )
        }
    }

private fun getTappedPointIndex(
    canvasPoints: List<GraphDataPoint>,
    canvasTapPos: Offset
) = canvasPoints
    .mapIndexed { index, point ->
        index to (Offset(point.x, point.y) - canvasTapPos).getDistance()
    }
    .filter { (_, distance) ->
        distance < 200
    }
    .minByOrNull { (_, distance) ->
        distance
    }?.first

@Preview
@Composable
fun GraphPreview() {
    OrkoutTheme {
        Surface(color = MaterialTheme.colors.background) {
            Graph(
                points = listOf(
                    1f to 2f,
                    2f to 4.5f,
                    4f to 4f,
                    5f to 5f,
                    6f to 8f,
                    9f to 9f,
                ).map { GraphDataPoint(it.first, it.second, label = "test") },
                modifier = Modifier
                    .size(400.dp)
                    .padding(2.dp),
                contentPadding = PaddingValues(20.dp)
            )
        }
    }
}