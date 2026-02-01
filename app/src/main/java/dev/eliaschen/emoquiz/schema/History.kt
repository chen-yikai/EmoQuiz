package dev.eliaschen.emoquiz.schema

data class History(
    val id: String,
    val timestamp: Long,
    val totalQuestionCount: Int,
    val difficulty: Set<String>,
    val score: Int,
    val correctCount: Int,
    val wrongCount: Int
)