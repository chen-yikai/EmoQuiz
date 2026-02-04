package dev.eliaschen.emoquiz.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.eliaschen.emoquiz.LocalGameDataViewModel
import dev.eliaschen.emoquiz.LocalNavViewModel
import dev.eliaschen.emoquiz.component.AppCursor
import dev.eliaschen.emoquiz.schema.Question
import dev.eliaschen.emoquiz.toDifficultyLabel
import dev.eliaschen.emoquiz.viewmodel.GameColor
import dev.eliaschen.emoquiz.viewmodel.GameDataViewModel
import dev.eliaschen.emoquiz.viewmodel.Screen

@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    val game = LocalGameDataViewModel.current
    val nav = LocalNavViewModel.current


    game.apply {
        val questionCount = currentIndex + 1
        val totalQuestionCount = gameQuestions.size
        val question = gameQuestions[game.currentIndex]

        LaunchedEffect(currentIndex) {
            lockSubmit = false
            color = GameColor.NORMAL
            selectedOptionsId = 0
        }

        LaunchedEffect(gameOver) {
            if (gameOver) nav.navTo(Screen.History)
        }

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding()
                .fillMaxSize()
        ) {
            PenaltyProgressBox(modifier = Modifier.align(Alignment.TopCenter))
            AnimatedContent(
                question, transitionSpec = {
                    (slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()).using(
                        SizeTransform(clip = false)
                    )
                },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    QuestionInfo(
                        question.difficulty.toDifficultyLabel(),
                        it,
                        questionCount,
                        totalQuestionCount
                    )
                    OptionsGridLayout(it)
                }
            }
            ScoreBox(modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun GameDataViewModel.OptionsGridLayout(question: Question) {
    LazyVerticalGrid(
        GridCells.Fixed(2),
        modifier = Modifier.widthIn(max = 350.dp),
    ) {
        items(question.options) { item ->
            val boxShape = RoundedCornerShape(10.dp)
            var boxRect by remember { mutableStateOf(Rect.Zero) }
            var isHovered by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(if (isHovered) 1.1f else 1f)
            val progress = remember { Animatable(0f) }

            LaunchedEffect(cursorRect, boxRect) {
                isHovered = boxRect.overlaps(cursorRect) && !lockSubmit
            }

            LaunchedEffect(isHovered) {
                if (isHovered) {
                    progress.animateTo(1f, tween(1500, easing = LinearEasing))
                    handleSubmit(item.id)
                } else {
                    progress.snapTo(0f)
                }
            }

            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .onGloballyPositioned { layoutCoordinates ->
                        boxRect = layoutCoordinates.boundsInWindow()
                    }
                    .scale(scale)
                    .background(
                        if (selectedOptionsId == item.id) color.containerColor else GameColor.NORMAL.containerColor,
                        boxShape
                    )
                    .border(
                        1.dp,
                        if (selectedOptionsId == item.id) color.borderColor else GameColor.NORMAL.borderColor,
                        boxShape
                    )
                    .clip(boxShape)
                    .then(if (isHovered) Modifier.drawBehind {
                        drawLine(
                            color = Color(0xFFE088EB).copy(0.5f),
                            Offset(0f, size.height / 2),
                            Offset(size.width * progress.value, size.height / 2),
                            strokeWidth = size.height
                        )
                    } else Modifier)
//                    .clickable(
//                        enabled = !lockSubmit,
//                        interactionSource = remember { MutableInteractionSource() },
//                        indication = ripple(),
//                        onClick = {
//                            handleSubmit(item.id)
//                        })
                    .padding(18.dp), contentAlignment = Alignment.Center
            ) {
                Text(item.title)
            }
        }
    }
}

@Composable
private fun GameDataViewModel.PenaltyProgressBox(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(color) {
        if (color == GameColor.REJECT) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis = 5000, easing = LinearEasing))
            lockSubmit = false
            color = GameColor.NORMAL
        }
    }

    AnimatedVisibility(
        color == GameColor.REJECT,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                """
                    答案錯誤
                    你已被暫停作答，請等待5秒後再試一次
                """.trimIndent(), color = GameColor.REJECT.borderColor
            )
        }
    }
}

@Composable
private fun QuestionInfo(
    difficultyLabel: String,
    question: Question,
    questionCount: Int,
    totalQuestionCount: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Card(
            shape = RoundedCornerShape(5.dp),
        ) {
            Text(
                difficultyLabel,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                fontSize = 15.sp
            )
        }
        Text(question.content, fontWeight = FontWeight.Medium, fontSize = 25.sp)
        Text("$questionCount/$totalQuestionCount", color = Color.Gray)
    }
}

@Composable
private fun GameDataViewModel.ScoreBox(modifier: Modifier = Modifier) {
    val backgroundColor by animateColorAsState(
        if (color == GameColor.NORMAL) MaterialTheme.colorScheme.secondaryContainer else color.containerColor,
        label = "bottom score box animation"
    )

    BackHandler {
        handleExit()
    }

    Row(
        modifier
            .fillMaxWidth()
            .padding(20.dp)
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedContent(color, transitionSpec = {
            (slideInVertically { it } + fadeIn() togetherWith slideOutVertically() { -it } + fadeOut()).using(
                SizeTransform(clip = false)
            )
        }) {
            when (it) {
                GameColor.NORMAL -> Text("目前分數")
                GameColor.PASS -> Text("得分")
                GameColor.REJECT -> Text("扣分")
            }
        }
        AnimatedContent(color, transitionSpec = {
            (slideInVertically { it } + fadeIn() togetherWith slideOutVertically() { -it } + fadeOut()).using(
                SizeTransform(clip = false)
            )
        }) {
            Box(modifier = Modifier.width(20.dp), contentAlignment = Alignment.CenterEnd) {
                if (it == GameColor.NORMAL) {
                    Text(score.toString())
                } else {
                    Text("${if (it == GameColor.REJECT) "-" else "+"}$scoreChange")
                }
            }
        }
    }
}
