package dev.eliaschen.emoquiz.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.eliaschen.emoquiz.LocalGameDataViewModel
import dev.eliaschen.emoquiz.LocalNavViewModel
import dev.eliaschen.emoquiz.R
import dev.eliaschen.emoquiz.viewmodel.Screen
import kotlinx.coroutines.delay

@Composable
fun CustomScaffold(content: @Composable () -> Unit) {
    val nav = LocalNavViewModel.current
    val game = LocalGameDataViewModel.current

    Scaffold {
        Box(modifier = Modifier.padding(it)) {
            content()
            if (nav.currentStack != Screen.Game) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.BottomCenter)
                        .shadow(5.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .padding(10.dp)
                ) {
                    Screen.entries.forEach { navItem ->
                        val isSelected = nav.currentStack == navItem
                        if (navItem.activeIcon != 0) {
                            CursorOutlineButton(
                                onClick = {
                                    nav.navTo(navItem)
                                },
                                selected = isSelected,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 7.dp),
                                style = defaultCursorButtonColors(
                                    shape = RoundedCornerShape(100f),
                                    containerColor = Color.Transparent
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(if (isSelected) navItem.activeIcon else navItem.inactiveIcon),
                                        contentDescription = null, modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.secondaryContainer,
                                                CircleShape
                                            )
                                            .padding(horizontal = 15.dp, vertical = 2.dp)
                                    )
                                    Text(navItem.title, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(bottom = 80.dp)
                    .padding(horizontal = 20.dp),
            ) {
                when (nav.currentStack) {
                    Screen.Game -> GoBackButton()
                    Screen.History -> ScrollControlButtons()
                    else -> {}
                }
            }
            AppCursor()
            Card(
                onClick = {
                    game.apply {
                        adjustCursor = !adjustCursor
                    }
                },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(5.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(bottom = 80.dp)
                    .padding(horizontal = 20.dp)
                    .zIndex(1f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.adjust),
                        contentDescription = null
                    )
                    Text("校正游標", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun RowScope.ScrollControlButtons() {
    val game = LocalGameDataViewModel.current
    val nav = LocalNavViewModel.current

    if (nav.currentStack == Screen.History) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ScrollHoverButton(
                listState = game.historyListState,
                icon = Icons.Default.KeyboardArrowUp,
                contentDescription = "向上捲動",
                scrollUp = true
            )
            ScrollHoverButton(
                listState = game.historyListState,
                icon = Icons.Default.KeyboardArrowDown,
                contentDescription = "向下捲動",
                scrollUp = false
            )
        }
    }
}

@Composable
private fun ScrollHoverButton(
    listState: LazyListState,
    icon: ImageVector,
    contentDescription: String,
    scrollUp: Boolean
) {
    val game = LocalGameDataViewModel.current
    var boxRect by remember { mutableStateOf(Rect.Zero) }
    val isHover by remember { derivedStateOf { game.cursorRect.overlaps(boxRect) } }
    val scale by animateFloatAsState(if (isHover) 1.1f else 1f)
    val enable = if (scrollUp) listState.canScrollBackward else listState.canScrollForward

    LaunchedEffect(isHover) {
        while (isHover) {
            val itemCount = listState.layoutInfo.totalItemsCount
            if (itemCount > 0) {
                val targetIndex = if (scrollUp) {
                    (listState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                } else {
                    (listState.firstVisibleItemIndex + 1).coerceAtMost(itemCount - 1)
                }
                listState.animateScrollToItem(targetIndex)
            }
            delay(100)
        }
    }

    Box(
        modifier = Modifier
            .scale(if (!enable) 1f else scale)
            .onGloballyPositioned { boxRect = it.boundsInWindow() }
            .shadow(5.dp, CircleShape)
            .background(
                if (!enable) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondaryContainer,
                CircleShape
            )
            .padding(15.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isHover) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
            )
            AnimatedVisibility(isHover && enable) {
                Text(contentDescription, fontSize = 13.sp)
            }
        }
    }
}


@Composable
private fun RowScope.GoBackButton() {
    val game = LocalGameDataViewModel.current

    CursorOutlineButton(onClick = {
        game.handleExit()
    }, modifier = Modifier) { isHover ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = null,
            )
            AnimatedVisibility(isHover) {
                Text("結束遊戲")
            }
        }
    }
}