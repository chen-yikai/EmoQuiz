package dev.eliaschen.emoquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import dev.eliaschen.emoquiz.screen.NavGraph
import dev.eliaschen.emoquiz.viewmodel.GameDataViewModel
import dev.eliaschen.emoquiz.viewmodel.NavViewModel
import dev.eliaschen.emoquiz.viewmodel.QuestionViewModel

val LocalNavViewModel =
    compositionLocalOf<NavViewModel> { error("NavViewModel not provided") }
val LocalGameDataViewModel = compositionLocalOf<GameDataViewModel> { error("GameViewModel not provided") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
                val nav: NavViewModel by viewModels()
                val gameData: GameDataViewModel by viewModels()

                CompositionLocalProvider(
                    LocalNavViewModel provides nav,
                    LocalGameDataViewModel provides gameData
                ) {
                    NavGraph()
                }
            }
    }
}