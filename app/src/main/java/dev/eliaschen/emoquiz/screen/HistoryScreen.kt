package dev.eliaschen.emoquiz.screen

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.eliaschen.emoquiz.LocalGameDataViewModel
import dev.eliaschen.emoquiz.R
import dev.eliaschen.emoquiz.toDateTimeFormat
import dev.eliaschen.emoquiz.toDifficultyLabel
import dev.eliaschen.emoquiz.viewmodel.GameColor
import kotlinx.coroutines.delay

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    val game = LocalGameDataViewModel.current
    var isFlip by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition()
    val spotlightTransition by infiniteTransition.animateColor(
        Color.White, MaterialTheme.colorScheme.primaryContainer,
        infiniteRepeatable(
            animation = tween(100, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        delay(1000)
        game.spotlightId = ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 10.dp)
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("遊玩紀錄", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { isFlip = !isFlip }) {
                Icon(
                    painter = painterResource(R.drawable.swap),
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer {
                        scaleX = if (isFlip) -1f else 1f
                    })
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(if (!isFlip) game.histories.sortedByDescending { it.timestamp } else game.histories.sortedBy { it.timestamp }) { history ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (history.id == game.spotlightId) Color(
                            spotlightTransition.value
                        ) else Color.White
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Column {
                            history.timestamp.apply {
                                Text(
                                    toDateTimeFormat("MM月dd號 EE"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    toDateTimeFormat("hh:mm a"),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(Modifier.height(5.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                    shape = RoundedCornerShape(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(
                                            vertical = 3.dp,
                                            horizontal = 5.dp
                                        ), contentAlignment = Alignment.Center
                                    ) {
                                        Text("${history.totalQuestionCount}題")
                                    }
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                    shape = RoundedCornerShape(5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            vertical = 3.dp,
                                            horizontal = 5.dp
                                        )
                                    ) {
                                        history.difficulty.forEachIndexed { index, text ->
                                            Text(text.toDifficultyLabel())
                                            if (index != history.difficulty.size - 1) {
                                                Text(", ")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                "${history.score}分",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "對${history.correctCount}題",
                                    color = GameColor.PASS.borderColor
                                )
                                Text("/", color = Color.Gray.copy(0.5f))
                                Text(
                                    "錯${history.wrongCount}題",
                                    color = GameColor.REJECT.borderColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}