package dev.eliaschen.emoquiz.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import dev.eliaschen.emoquiz.readAssetFile
import dev.eliaschen.emoquiz.schema.DataWrapper
import dev.eliaschen.emoquiz.schema.Question
import dev.eliaschen.emoquiz.schema.Score
import dev.eliaschen.emoquiz.toJson

open class QuestionViewModel(private val context: Application) : AndroidViewModel(context) {
    var questions = mutableStateListOf<Question>()
        private set
    var scoreRules by mutableStateOf(Score(0, 0, 0, 0))
        private set

    init {
        val questionData = context.readAssetFile("questions.json").toJson<DataWrapper>()
        questions.addAll(questionData.data)
        scoreRules = questionData.score
        Log.i("QuestionModel", "Data loaded")
    }
}
