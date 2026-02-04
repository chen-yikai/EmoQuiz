package dev.eliaschen.emoquiz.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.eliaschen.emoquiz.LocalGameDataViewModel
import dev.eliaschen.emoquiz.LocalNavViewModel
import dev.eliaschen.emoquiz.R
import dev.eliaschen.emoquiz.viewmodel.Screen

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
                                style = DefaultCursorButton().copy(
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
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 80.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoBackButton()
                Card(
                    onClick = {
                        game.apply {
                            adjustCursor = !adjustCursor
                        }
                    },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.elevatedCardElevation(5.dp),
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
                        Text("校正鼠標")
                    }
                }
            }
            AppCursor()
        }
    }
}

@Composable
fun RowScope.ScrollControlButtons() {

}


@Composable
private fun RowScope.GoBackButton() {
    val game = LocalGameDataViewModel.current
    val nav = LocalNavViewModel.current

    if (nav.currentStack == Screen.Game) {
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
    } else {
        Spacer(Modifier.weight(1f))
    }
}