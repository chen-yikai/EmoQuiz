package dev.eliaschen.emoquiz.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
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
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    style: DefaultCursorButton = DefaultCursorButton(),
    onClick: () -> Unit,
    content: @Composable (Boolean) -> Unit
) {
    val game = LocalGameDataViewModel.current
    var boxRect by remember { mutableStateOf(Rect.Zero) }
    val isHover by remember { derivedStateOf { game.cursorRect.overlaps(boxRect) } }
    val progress = remember { Animatable(0f) }
    val scale by animateFloatAsState(if (isHover) 1.1f else 1f)

    LaunchedEffect(isHover) {
        if (isHover) {
            progress.animateTo(1f, animationSpec = tween(1500, easing = LinearEasing))
            onClick()
        } else {
            progress.snapTo(0f)
        }
    }

    Box(
        Modifier
            .scale(scale)
            .onGloballyPositioned {
                boxRect = it.boundsInWindow()
            }
            .background(style.containerColor, style.shape)
            .borderProgressBar(
                if (!selected) progress.value else 1f,
                style.progressColor,
                3.dp,
                style.shape
            )
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        content(isHover)
    }
}

fun Modifier.borderProgressBar(
    progress: Float,
    color: Color,
    strokeWidth: Dp,
    shape: Shape
): Modifier = this.drawWithCache {
    val path = Path().apply {
        addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache))
    }
    val pathMeasure = PathMeasure()
    pathMeasure.setPath(path, false)
    val pathLength = pathMeasure.length
    val partialPath = Path()
    pathMeasure.getSegment(0f, progress * pathLength, partialPath, true)
    onDrawBehind {
        drawPath(
            path = partialPath,
            color = color,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}