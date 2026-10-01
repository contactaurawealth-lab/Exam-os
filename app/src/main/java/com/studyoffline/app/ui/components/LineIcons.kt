package com.studyoffline.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.studyoffline.app.ui.theme.StudyOfflineTheme

/**
 * Consistent 1.5dp stroke Line Icons adhering strictly to Design Spec §5
 */
object LineIcons {

    @Composable
    fun Home(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp,
        tint: Color = StudyOfflineTheme.colors.textPrimary
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.5.dp.toPx()
            val w = this.size.width
            val h = this.size.height

            val path = Path().apply {
                moveTo(w * 0.15f, h * 0.42f)
                lineTo(w * 0.5f, h * 0.15f)
                lineTo(w * 0.85f, h * 0.42f)
                lineTo(w * 0.85f, h * 0.85f)
                lineTo(w * 0.15f, h * 0.85f)
                close()
            }
            drawPath(path, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

            val door = Path().apply {
                moveTo(w * 0.38f, h * 0.85f)
                lineTo(w * 0.38f, h * 0.58f)
                lineTo(w * 0.62f, h * 0.58f)
                lineTo(w * 0.62f, h * 0.85f)
            }
            drawPath(door, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    @Composable
    fun Book(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp,
        tint: Color = StudyOfflineTheme.colors.textPrimary
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.5.dp.toPx()
            val w = this.size.width
            val h = this.size.height

            val spine = Path().apply {
                moveTo(w * 0.5f, h * 0.25f)
                lineTo(w * 0.5f, h * 0.85f)
            }
            drawPath(spine, tint, style = Stroke(width = stroke, cap = StrokeCap.Round))

            val pages = Path().apply {
                // Left page
                moveTo(w * 0.5f, h * 0.3f)
                cubicTo(w * 0.35f, h * 0.22f, w * 0.25f, h * 0.22f, w * 0.12f, h * 0.26f)
                lineTo(w * 0.12f, h * 0.8f)
                cubicTo(w * 0.25f, h * 0.76f, w * 0.35f, h * 0.76f, w * 0.5f, h * 0.85f)

                // Right page
                cubicTo(w * 0.65f, h * 0.76f, w * 0.75f, h * 0.76f, w * 0.88f, h * 0.8f)
                lineTo(w * 0.88f, h * 0.26f)
                cubicTo(w * 0.75f, h * 0.22f, w * 0.65f, h * 0.22f, w * 0.5f, h * 0.3f)
            }
            drawPath(pages, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    @Composable
    fun Practice(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp,
        tint: Color = StudyOfflineTheme.colors.textPrimary
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.5.dp.toPx()
            val w = this.size.width
            val h = this.size.height

            // Double overlapping flashcards
            drawRoundRect(
                color = tint,
                topLeft = Offset(w * 0.12f, h * 0.25f),
                size = Size(w * 0.55f, h * 0.6f),
                cornerRadius = CornerRadius(4.dp.toPx()),
                style = Stroke(width = stroke)
            )

            val backCard = Path().apply {
                moveTo(w * 0.35f, h * 0.15f)
                lineTo(w * 0.82f, h * 0.15f)
                cubicTo(w * 0.86f, h * 0.15f, w * 0.88f, h * 0.17f, w * 0.88f, h * 0.21f)
                lineTo(w * 0.88f, h * 0.65f)
            }
            drawPath(backCard, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    @Composable
    fun Calendar(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp,
        tint: Color = StudyOfflineTheme.colors.textPrimary
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.5.dp.toPx()
            val w = this.size.width
            val h = this.size.height

            drawRoundRect(
                color = tint,
                topLeft = Offset(w * 0.15f, h * 0.22f),
                size = Size(w * 0.7f, h * 0.65f),
                cornerRadius = CornerRadius(4.dp.toPx()),
                style = Stroke(width = stroke)
            )

            // Header line
            drawLine(
                color = tint,
                start = Offset(w * 0.15f, h * 0.42f),
                end = Offset(w * 0.85f, h * 0.42f),
                strokeWidth = stroke
            )

            // Rings
            drawLine(tint, Offset(w * 0.32f, h * 0.14f), Offset(w * 0.32f, h * 0.25f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(tint, Offset(w * 0.68f, h * 0.14f), Offset(w * 0.68f, h * 0.25f), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }

    @Composable
    fun User(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp,
        tint: Color = StudyOfflineTheme.colors.textPrimary
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.5.dp.toPx()
            val w = this.size.width
            val h = this.size.height

            // Head circle
            drawCircle(
                color = tint,
                radius = w * 0.2f,
                center = Offset(w * 0.5f, h * 0.35f),
                style = Stroke(width = stroke)
            )

            // Body arc
            val body = Path().apply {
                moveTo(w * 0.2f, h * 0.85f)
                cubicTo(w * 0.2f, h * 0.65f, w * 0.35f, h * 0.62f, w * 0.5f, h * 0.62f)
                cubicTo(w * 0.65f, h * 0.62f, w * 0.8f, h * 0.65f, w * 0.8f, h * 0.85f)
            }
            drawPath(body, tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
        }
    }

    @Composable
    fun Flame(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp,
        tint: Color = StudyOfflineTheme.colors.warning
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.5.dp.toPx()
            val w = this.size.width
            val h = this.size.height

            val flame = Path().apply {
                moveTo(w * 0.5f, h * 0.12f)
                cubicTo(w * 0.72f, h * 0.28f, w * 0.85f, h * 0.55f, w * 0.82f, h * 0.72f)
                cubicTo(w * 0.78f, h * 0.88f, w * 0.65f, h * 0.92f, w * 0.5f, h * 0.92f)
                cubicTo(w * 0.35f, h * 0.92f, w * 0.22f, h * 0.88f, w * 0.18f, h * 0.72f)
                cubicTo(w * 0.15f, h * 0.52f, w * 0.32f, h * 0.35f, w * 0.42f, h * 0.38f)
                cubicTo(w * 0.38f, h * 0.48f, w * 0.45f, h * 0.55f, w * 0.52f, h * 0.5f)
                cubicTo(w * 0.58f, h * 0.45f, w * 0.58f, h * 0.32f, w * 0.5f, h * 0.12f)
            }
            drawPath(flame, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    @Composable
    fun Plus(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp,
        tint: Color = StudyOfflineTheme.colors.onAccent
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.8.dp.toPx()
            val w = this.size.width
            val h = this.size.height
            drawLine(tint, Offset(w * 0.5f, h * 0.2f), Offset(w * 0.5f, h * 0.8f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(tint, Offset(w * 0.2f, h * 0.5f), Offset(w * 0.8f, h * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }

    @Composable
    fun Check(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        tint: Color = StudyOfflineTheme.colors.success
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.8.dp.toPx()
            val w = this.size.width
            val h = this.size.height
            val path = Path().apply {
                moveTo(w * 0.2f, h * 0.52f)
                lineTo(w * 0.42f, h * 0.75f)
                lineTo(w * 0.82f, h * 0.28f)
            }
            drawPath(path, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    @Composable
    fun Close(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        tint: Color = StudyOfflineTheme.colors.error
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.8.dp.toPx()
            val w = this.size.width
            val h = this.size.height
            drawLine(tint, Offset(w * 0.25f, h * 0.25f), Offset(w * 0.75f, h * 0.75f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(tint, Offset(w * 0.75f, h * 0.25f), Offset(w * 0.25f, h * 0.75f), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }

    @Composable
    fun ArrowLeft(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp,
        tint: Color = StudyOfflineTheme.colors.textPrimary
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.6.dp.toPx()
            val w = this.size.width
            val h = this.size.height
            val path = Path().apply {
                moveTo(w * 0.6f, h * 0.22f)
                lineTo(w * 0.32f, h * 0.5f)
                lineTo(w * 0.6f, h * 0.78f)
            }
            drawPath(path, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    @Composable
    fun ArrowRight(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp,
        tint: Color = StudyOfflineTheme.colors.textSecondary
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.6.dp.toPx()
            val w = this.size.width
            val h = this.size.height
            val path = Path().apply {
                moveTo(w * 0.4f, h * 0.22f)
                lineTo(w * 0.68f, h * 0.5f)
                lineTo(w * 0.4f, h * 0.78f)
            }
            drawPath(path, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    @Composable
    fun Timer(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp,
        tint: Color = StudyOfflineTheme.colors.textPrimary
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.5.dp.toPx()
            val w = this.size.width
            val h = this.size.height

            drawCircle(
                color = tint,
                radius = w * 0.38f,
                center = Offset(w * 0.5f, h * 0.54f),
                style = Stroke(width = stroke)
            )

            // Top button
            drawLine(tint, Offset(w * 0.42f, h * 0.12f), Offset(w * 0.58f, h * 0.12f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(tint, Offset(w * 0.5f, h * 0.12f), Offset(w * 0.5f, h * 0.18f), strokeWidth = stroke, cap = StrokeCap.Round)

            // Hands
            drawLine(tint, Offset(w * 0.5f, h * 0.54f), Offset(w * 0.5f, h * 0.34f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(tint, Offset(w * 0.5f, h * 0.54f), Offset(w * 0.66f, h * 0.54f), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }

    @Composable
    fun Play(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp,
        tint: Color = StudyOfflineTheme.colors.onAccent
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.6.dp.toPx()
            val w = this.size.width
            val h = this.size.height
            val path = Path().apply {
                moveTo(w * 0.32f, h * 0.22f)
                lineTo(w * 0.75f, h * 0.5f)
                lineTo(w * 0.32f, h * 0.78f)
                close()
            }
            drawPath(path, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    @Composable
    fun Pause(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp,
        tint: Color = StudyOfflineTheme.colors.onAccent
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.8.dp.toPx()
            val w = this.size.width
            val h = this.size.height
            drawLine(tint, Offset(w * 0.36f, h * 0.24f), Offset(w * 0.36f, h * 0.76f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(tint, Offset(w * 0.64f, h * 0.24f), Offset(w * 0.64f, h * 0.76f), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }

    @Composable
    fun Flag(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        tint: Color = StudyOfflineTheme.colors.warning
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.5.dp.toPx()
            val w = this.size.width
            val h = this.size.height

            // Flag pole
            drawLine(tint, Offset(w * 0.22f, h * 0.15f), Offset(w * 0.22f, h * 0.88f), strokeWidth = stroke, cap = StrokeCap.Round)

            // Flag cloth
            val path = Path().apply {
                moveTo(w * 0.22f, h * 0.18f)
                lineTo(w * 0.78f, h * 0.35f)
                lineTo(w * 0.22f, h * 0.52f)
            }
            drawPath(path, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    @Composable
    fun Settings(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp,
        tint: Color = StudyOfflineTheme.colors.textPrimary
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.5.dp.toPx()
            val w = this.size.width
            val h = this.size.height

            drawCircle(tint, radius = w * 0.15f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(stroke))
            drawCircle(tint, radius = w * 0.36f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(stroke))

            // Spokes
            for (i in 0 until 6) {
                val angle = Math.toRadians(i * 60.0)
                val cos = Math.cos(angle).toFloat()
                val sin = Math.sin(angle).toFloat()
                drawLine(
                    color = tint,
                    start = Offset(w * 0.5f + cos * w * 0.32f, h * 0.5f + sin * h * 0.32f),
                    end = Offset(w * 0.5f + cos * w * 0.44f, h * 0.5f + sin * h * 0.44f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }
    }

    @Composable
    fun Trash(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        tint: Color = StudyOfflineTheme.colors.error
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.5.dp.toPx()
            val w = this.size.width
            val h = this.size.height

            // Lid line
            drawLine(tint, Offset(w * 0.15f, h * 0.3f), Offset(w * 0.85f, h * 0.3f), strokeWidth = stroke, cap = StrokeCap.Round)
            // Handle
            drawLine(tint, Offset(w * 0.4f, h * 0.2f), Offset(w * 0.6f, h * 0.2f), strokeWidth = stroke, cap = StrokeCap.Round)

            // Can
            val can = Path().apply {
                moveTo(w * 0.24f, h * 0.3f)
                lineTo(w * 0.28f, h * 0.84f)
                cubicTo(w * 0.29f, h * 0.88f, w * 0.33f, h * 0.88f, w * 0.36f, h * 0.88f)
                lineTo(w * 0.64f, h * 0.88f)
                cubicTo(w * 0.67f, h * 0.88f, w * 0.71f, h * 0.88f, w * 0.72f, h * 0.84f)
                lineTo(w * 0.76f, h * 0.3f)
            }
            drawPath(can, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    @Composable
    fun Lock(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp,
        tint: Color = StudyOfflineTheme.colors.textPrimary
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.5.dp.toPx()
            val w = this.size.width
            val h = this.size.height

            // Body
            drawRoundRect(
                color = tint,
                topLeft = Offset(w * 0.2f, h * 0.44f),
                size = Size(w * 0.6f, h * 0.46f),
                cornerRadius = CornerRadius(w * 0.08f, h * 0.08f),
                style = Stroke(width = stroke)
            )

            // Shackle
            val shackle = Path().apply {
                moveTo(w * 0.32f, h * 0.44f)
                lineTo(w * 0.32f, h * 0.28f)
                cubicTo(w * 0.32f, h * 0.16f, w * 0.68f, h * 0.16f, w * 0.68f, h * 0.28f)
                lineTo(w * 0.68f, h * 0.44f)
            }
            drawPath(shackle, tint, style = Stroke(width = stroke, cap = StrokeCap.Round))

            // Keyhole dot
            drawCircle(tint, radius = stroke * 1.2f, center = Offset(w * 0.5f, h * 0.64f))
        }
    }

    @Composable
    fun Search(
        modifier: Modifier = Modifier,
        size: Dp = 20.dp,
        tint: Color = StudyOfflineTheme.colors.textSecondary
    ) {
        Canvas(modifier = modifier.size(size)) {
            val stroke = 1.5.dp.toPx()
            val w = this.size.width
            val h = this.size.height

            drawCircle(
                color = tint,
                radius = w * 0.3f,
                center = Offset(w * 0.42f, h * 0.42f),
                style = Stroke(width = stroke)
            )
            drawLine(
                color = tint,
                start = Offset(w * 0.64f, h * 0.64f),
                end = Offset(w * 0.86f, h * 0.86f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}
