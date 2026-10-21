package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

/** Rotate the colors inside a fixed rounded border without rotating the card or its content. */
fun Modifier.animatedRainbowBorder(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "daily_verse_rainbow")
    val rotation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6_000, easing = LinearEasing), RepeatMode.Restart),
        label = "rainbow_rotation"
    )
    drawWithCache {
        val width = 3.dp.toPx()
        val radius = 16.dp.toPx()
        val border = Path().apply {
            fillType = PathFillType.EvenOdd
            addRoundRect(RoundRect(Rect(0f, 0f, size.width, size.height), CornerRadius(radius)))
            addRoundRect(RoundRect(
                Rect(width, width, size.width - width, size.height - width),
                CornerRadius((radius - width).coerceAtLeast(0f))
            ))
        }
        val rainbow = Brush.sweepGradient(listOf(
            Color(0xFFFF4040), Color(0xFFFF9800), Color(0xFFFFD600),
            Color(0xFF43C75A), Color(0xFF2196F3), Color(0xFF6746DB),
            Color(0xFFCF42DE), Color(0xFFFF4040)
        ), center = Offset(size.width / 2f, size.height / 2f))
        onDrawWithContent {
            drawContent()
            clipPath(border) {
                rotate(rotation.value) {
                    drawCircle(rainbow, radius = size.maxDimension)
                }
            }
        }
    }
}
