package dev.eliaschen.emoquiz.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import dev.eliaschen.emoquiz.LocalNavViewModel
import dev.eliaschen.emoquiz.viewmodel.Screen

@Composable
fun CustomScaffold(content: @Composable () -> Unit) {
    val nav = LocalNavViewModel.current

    Scaffold(bottomBar = {
        if (nav.currentStack != Screen.Game) {
            NavigationBar {
                Screen.entries.forEach { navItem ->
                    val isSelected = nav.currentStack == navItem
                    if (navItem.activeIcon != 0) {
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                nav.navTo(navItem)
                            }, icon = {
                                Icon(
                                    painter = painterResource(if (isSelected) navItem.activeIcon else navItem.inactiveIcon),
                                    contentDescription = null
                                )
                            }, label = { Text(navItem.title) })
                    }
                }
            }
        }
    }) {
        Surface(modifier = Modifier.padding(it)) {
            content()
        }
    }
}