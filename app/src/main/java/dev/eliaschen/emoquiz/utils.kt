package dev.eliaschen.emoquiz

import android.app.Application
import androidx.compose.ui.util.fastFirst
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Locale

object Utils {
    val gson = Gson()
}

inline fun <reified T> String.toJson(): T {
    val type = object : TypeToken<T>() {}.type
    val json = Utils.gson.fromJson<T>(this, type)
    return json
}

fun Application.readAssetFile(path: String): String {
    return this.assets.open(path).bufferedReader().readText()
}

fun Long.toDateTimeFormat(format: String): String {
    return SimpleDateFormat(format, Locale.TAIWAN).format(this)
}

val difficulty = listOf(Pair("簡單", "easy"), Pair("普通", "medium"), Pair("困難", "hard"))


fun String.toDifficultyLabel(): String {
    return difficulty.fastFirst { it.second == this }.first
}