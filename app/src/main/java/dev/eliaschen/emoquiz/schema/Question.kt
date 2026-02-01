package dev.eliaschen.emoquiz.schema

data class DataWrapper(
    val score: Score,
    val data: List<Question>,
)

data class Score(
    val easy: Int,
    val medium: Int,
    val hard: Int,
    val penalty: Int,
)

data class Question(
    val id: Int,
    val content: String,
    val difficulty: String,
    val options: List<Option>,
    val correctAnswer: List<Int>
)

data class Option(
    val id: Int,
    val title: String,
)
