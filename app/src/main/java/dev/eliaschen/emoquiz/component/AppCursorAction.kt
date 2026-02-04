package dev.eliaschen.emoquiz.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.eliaschen.emoquiz.LocalGameDataViewModel

@Composable
fun CursorOutlineButton(
    shape: Shape,
    modifier: Modifier = Modifier,
    content: @Composable (Boolean) -> Unit
) {
    val game = LocalGameDataViewModel.current
    var isHover by remember { mutableStateOf(false) }
    var boxRect by remember { mutableStateOf(Rect.Zero) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(game.cursorRect, boxRect) {
        isHover = game.cursorRect.overlaps(boxRect)
        if (isHover) {
            progress.animateTo(1f, animationSpec = tween(2000, easing = LinearEasing))
        } else {
            progress.snapTo(0f)
        }
    }

    Box(
        modifier
            .borderProgressBar(progress.value, Color(0xFFE088EB), 1.dp, shape)
            .onGloballyPositioned {
                boxRect = it.boundsInWindow()
            }
    ) {
        content(isHover)
    }
}

fun Modifier.borderProgressBar(
    progress: Float,
    color: Color,
    strokeWidth: Dp,
    shape: Shape
): Modifier = this.drawWithContent {
    drawContent()
    val path = Path().apply {
        addOutline(shape.createOutline(size, layoutDirection, this@drawWithContent))
    }
    val pathMeasure = PathMeasure()
    pathMeasure.setPath(path, false)
    val pathLength = pathMeasure.length
    val partialPath = Path()
    pathMeasure.getSegment(0f, progress * pathLength, partialPath, true)
    drawPath(
        path = partialPath,
        color = color,
        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
    )
}