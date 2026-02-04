package dev.eliaschen.emoquiz.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import dev.eliaschen.emoquiz.LocalGameDataViewModel

data class DefaultCursorButton(
    val containerColor: Color = Color(0xFFE8DEF8),
    val progressColor: Color = Color(0xFFCBA6F7),
    val shape: Shape = RoundedCornerShape(20f)
)

@Composable
fun CursorButton(
    modifier: Modifier = Modifier,
    colors: DefaultCursorButton = DefaultCursorButton(),
    onClick: () -> Unit,
    content: @Composable (Boolean) -> Unit
) {
    val game = LocalGameDataViewModel.current
    var isHover by remember { mutableStateOf(false) }
    var boxRect by remember { mutableStateOf(Rect.Zero) }
    val progress = remember { Animatable(0f) }
    val scale by animateFloatAsState(if (isHover) 1.1f else 1f)

    LaunchedEffect(game.cursorRect, boxRect) {
        isHover = game.cursorRect.overlaps(boxRect)
        if (isHover) {
            progress.animateTo(1f, animationSpec = tween(1500, easing = LinearEasing))
            onClick()
        } else {
            progress.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(colors.shape)
            .background(colors.containerColor)
            .then(modifier)
            .onGloballyPositioned {
                boxRect = it.boundsInWindow()
            }
            .then(if (isHover) Modifier.drawWithCache {
                onDrawBehind {
                    drawLine(
                        colors.progressColor,
                        Offset(0f, 0.5f * size.height),
                        Offset(size.width * progress.value, 0.5f * size.height),
                        strokeWidth = size.height
                    )
                }
            } else Modifier), contentAlignment = Alignment.Center
    ) {
        content(isHover)
    }
}