package dev.eliaschen.emoquiz.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
    val containerColor: Color,
    val progressColor: Color,
    val selectedColor: Color,
    val shape: Shape = RoundedCornerShape(30f)
)

@Composable
fun defaultCursorButtonColors(
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    progressColor: Color = MaterialTheme.colorScheme.secondary.copy(0.5f),
    selectedColor: Color = MaterialTheme.colorScheme.primaryContainer,
    shape: Shape = RoundedCornerShape(30f)
) = DefaultCursorButton(containerColor, progressColor, selectedColor, shape)

@Composable
fun CursorButton(
    modifier: Modifier = Modifier,
    colors: DefaultCursorButton = defaultCursorButtonColors(),
    selected: Boolean = false,
    onClick: () -> Unit,
    content: @Composable (Boolean) -> Unit
) {
    val game = LocalGameDataViewModel.current
    var boxRect by remember { mutableStateOf(Rect.Zero) }
    val isHover by remember(game.cursorRect) { derivedStateOf { game.cursorRect.overlaps(boxRect) } }
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
        modifier = Modifier
            .scale(scale)
            .clip(colors.shape)
            .background(colors.containerColor)
            .then(modifier)
            .then(
                if (selected) Modifier.border(
                    3.dp,
                    MaterialTheme.colorScheme.primary,
                    colors.shape
                ) else Modifier,
            )
            .onGloballyPositioned {
                boxRect = it.boundsInWindow()
            }
            .then(if (isHover) Modifier.drawBehind {
                drawLine(
                    colors.progressColor,
                    Offset(0f, 0.5f * size.height),
                    Offset(size.width * progress.value, 0.5f * size.height),
                    strokeWidth = size.height
                )
            } else Modifier), contentAlignment = Alignment.Center
    ) {
        content(isHover)
    }
}