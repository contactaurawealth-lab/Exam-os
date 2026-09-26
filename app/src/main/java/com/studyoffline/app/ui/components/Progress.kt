package com.studyoffline.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.studyoffline.app.ui.theme.StudyOfflineTheme

@Composable
fun StudyLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = StudyOfflineTheme.colors.accent,
    trackColor: Color = StudyOfflineTheme.colors.surfaceMuted,
    height: Dp = 4.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(400),
        label = "LinearProgressAnimation"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val strokeWidth = size.height
        val y = size.height / 2f

        // Draw track
        drawLine(
            color = trackColor,
            start = Offset(strokeWidth / 2f, y),
            end = Offset(size.width - strokeWidth / 2f, y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Draw progress
        if (animatedProgress > 0f) {
            val progressWidth = (size.width - strokeWidth) * animatedProgress
            drawLine(
                color = color,
                start = Offset(strokeWidth / 2f, y),
                end = Offset((strokeWidth / 2f) + progressWidth, y),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun StudyCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 6.dp,
    progressColor: Color = StudyOfflineTheme.colors.accent,
    trackColor: Color = StudyOfflineTheme.colors.surfaceMuted,
    content: (@Composable () -> Unit)? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(400),
        label = "CircularProgressAnimation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val radius = (size.toPx() - strokePx) / 2f
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            val arcSize = Size(radius * 2f, radius * 2f)

            // Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress
            if (animatedProgress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        if (content != null) {
            content()
        }
    }
}
