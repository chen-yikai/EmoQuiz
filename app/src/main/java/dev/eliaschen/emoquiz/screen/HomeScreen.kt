package dev.eliaschen.emoquiz.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.eliaschen.emoquiz.LocalGameDataViewModel
import dev.eliaschen.emoquiz.LocalNavViewModel
import dev.eliaschen.emoquiz.LocalQuestionViewModel
import dev.eliaschen.emoquiz.R
import dev.eliaschen.emoquiz.difficulty
import dev.eliaschen.emoquiz.viewmodel.Screen

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val nav = LocalNavViewModel.current
    val game = LocalGameDataViewModel.current
    val questions = game.questions
    val context = LocalContext.current
    var questionCount by remember { mutableStateOf("") }
    val selectedDifficulty = remember { mutableStateSetOf<String>() }

    fun handleSubmit() {
        try {
            val count =
                questionCount.trim().toIntOrNull() ?: throw Exception("總題數請輸入數字")
            if (count != 0 && count > questions.count()) throw Exception("無效的總題數")
            if (selectedDifficulty.isEmpty()) throw Exception("請至少選擇一項難易度")
            game.generateQuestion(count, selectedDifficulty)
            nav.navTo(Screen.Game)
        } catch (e: Exception) {
            Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(R.drawable.logo_bg_removed), contentDescription = null)
            Text("題目選項", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier
                .widthIn(max = 200.dp)
                .padding(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    questionCount,
                    onValueChange = { questionCount = it },
                    label = { Text("總題數") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Column {
                    Text("難易度", fontSize = 15.sp)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        difficulty.forEach { (label, value) ->
                            val isSelected = selectedDifficulty.contains(value)
                            InputChip(
                                selected = isSelected,
                                onClick = {
                                    if (!isSelected) selectedDifficulty.add(value) else selectedDifficulty.remove(
                                        value
                                    )
                                },
                                label = { Text(label) })
                        }
                    }
                }
            }
            FloatingActionButton(onClick = {
                handleSubmit()
            }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Icon(painter = painterResource(R.drawable.play), contentDescription = null)
                    Text("開始遊戲")
                }
            }
        }
    }
}