package dev.eliaschen.emoquiz.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import dev.eliaschen.emoquiz.R

enum class Screen(
    val title: String,
    val activeIcon: Int = 0,
    val inactiveIcon: Int = 0
) {
    Home("開始遊戲", R.drawable.home, R.drawable.home_outline),
    History("遊玩紀錄", R.drawable.history, R.drawable.history),
    Game("遊戲")
}

class NavViewModel(private val context: Application) : AndroidViewModel(context) {
    private val initScreen = Screen.Home
    var currentStack by mutableStateOf(initScreen)
        private set

    fun navTo(screen: Screen) {
        currentStack = screen
    }
}