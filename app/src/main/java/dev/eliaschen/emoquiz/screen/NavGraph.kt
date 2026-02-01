package dev.eliaschen.emoquiz.screen

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import dev.eliaschen.emoquiz.LocalNavViewModel
import dev.eliaschen.emoquiz.component.CustomScaffold
import dev.eliaschen.emoquiz.viewmodel.Screen

@Composable
fun NavGraph() {
    val nav = LocalNavViewModel.current

    Crossfade(nav.currentStack){
        when (it) {
            Screen.Home ->
                CustomScaffold {
                    HomeScreen()
                }

            Screen.History -> CustomScaffold {
                HistoryScreen()
            }

            Screen.Game -> GameScreen()
        }
    }
}
