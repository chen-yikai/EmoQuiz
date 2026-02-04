package dev.eliaschen.emoquiz.viewmodel

import android.app.Application
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import dev.eliaschen.emoquiz.R
import dev.eliaschen.emoquiz.Utils
import dev.eliaschen.emoquiz.schema.History
import dev.eliaschen.emoquiz.schema.Question
import dev.eliaschen.emoquiz.toJson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import kotlin.math.absoluteValue

const val history_file = "history.json"

enum class GameColor(val containerColor: Color, val borderColor: Color) {
    NORMAL(Color.White, Color.Gray.copy(0.5f)),
    PASS(Color(0xFFE4F8E5), Color(0xFF75B472)),
    REJECT(Color(0xFFFEDEE5), Color(0xFF852221))
}

class GameDataViewModel(private val context: Application) : QuestionViewModel(context) {
    // Cursor
    var cursorRect by mutableStateOf(Rect.Zero)

    // History
    val historyFile = File(context.filesDir, history_file)
    val histories = mutableStateListOf<History>()
    var spotlightId by mutableStateOf("")

    // GameState
    val gameQuestions = mutableStateListOf<Question>()
    var score by mutableIntStateOf(0)
    var currentIndex by mutableIntStateOf(0)
    var gameOver by mutableStateOf(false)
    var lockSubmit by mutableStateOf(false)
    var scoreChange by mutableIntStateOf(0)
    var color by mutableStateOf(GameColor.NORMAL)
    var selectedOptionsId by mutableIntStateOf(0)
    var correctAnswer = mutableStateSetOf<Int>()
    var wrongAnswer = mutableStateSetOf<Int>()
    var gameDifficulty = mutableStateSetOf<String>()

    // SFX
    private val correctSFX = MediaPlayer.create(context, R.raw.duolingo)
    private val wrongSFX = MediaPlayer.create(context, R.raw.duolingo_wrong)

    fun resetGame() {
        gameQuestions.clear()
        score = 0
        currentIndex = 0
        gameOver = false
        lockSubmit = false
        color = GameColor.NORMAL
        selectedOptionsId = 0
        correctAnswer.clear()
        wrongAnswer.clear()
        gameDifficulty.clear()
    }

    fun String.toRewardScore(): Int {
        return when (this) {
            "easy" -> scoreRules.easy
            "medium" -> scoreRules.medium
            "hard" -> scoreRules.hard
            else -> 0
        }
    }

    init {
        loadHistory()
    }

    fun generateQuestion(total: Int, difficulty: Set<String>) {
        resetGame()
        val pool = questions.filter { it.difficulty in difficulty }
        gameDifficulty.addAll(difficulty)
        gameQuestions.addAll(pool.shuffled().take(total))
    }

    fun handleSubmit(id: Int) {
        lockSubmit = true
        selectedOptionsId = id
        val currentQuestion = gameQuestions[currentIndex]
        if (currentQuestion.correctAnswer.contains(id)) {
            // pass
            correctSFX.start()
            color = GameColor.PASS
            correctAnswer.add(currentQuestion.id)
            viewModelScope.launch {
                reward(gameQuestions[currentIndex].difficulty)
                delay(1000)
                if (currentIndex + 1 < gameQuestions.size) currentIndex += 1 else handleGameOver()
            }
        } else {
            // reject
            wrongSFX.start()
            color = GameColor.REJECT
            wrongAnswer.add(currentQuestion.id)
            penalty()
        }
    }

    fun handleExit() {
        gameOver = true
    }

    private fun reward(difficulty: String) {
        scoreChange = difficulty.toRewardScore()
        score += scoreChange
    }

    private fun penalty() {
        scoreChange = scoreRules.penalty.absoluteValue
        score -= scoreChange
    }

    private fun handleGameOver() {
        spotlightId = UUID.randomUUID().toString()
        val currentData = History(
            id = spotlightId,
            timestamp = System.currentTimeMillis(),
            totalQuestionCount = gameQuestions.size,
            difficulty = gameDifficulty,
            score = score.let { if (it < 0) 0 else score },
            correctCount = correctAnswer.size,
            wrongCount = wrongAnswer.size
        )
        histories.add(currentData)
        writeHistory()
        gameOver = true
    }

    private fun loadHistory() {
        if (!historyFile.exists()) historyFile.writeText("[]")
        histories.clear()
        histories.addAll(historyFile.readText().toJson())
    }

    private fun writeHistory() {
        historyFile.writeText(Utils.gson.toJson(histories))
    }
}