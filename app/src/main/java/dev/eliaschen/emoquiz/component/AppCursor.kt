package dev.eliaschen.emoquiz.component

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.eliaschen.emoquiz.LocalGameDataViewModel
import dev.eliaschen.emoquiz.LocalNavViewModel
import dev.eliaschen.emoquiz.R
import dev.eliaschen.emoquiz.viewmodel.Screen
import kotlin.math.roundToInt

@Composable
fun AppCursor() {
    val game = LocalGameDataViewModel.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val boxSize = with(density) { 50.dp.toPx() }
    val sensorManager =
        remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    var rawRoll by remember { mutableFloatStateOf(0f) }
    var rawPitch by remember { mutableFloatStateOf(0f) }

    var centerRoll by remember { mutableFloatStateOf(0f) }
    var centerPitch by remember { mutableFloatStateOf(0f) }

    val sensitivity = 3000f

    val animatedX by animateFloatAsState(
        targetValue = ((rawRoll - centerRoll) * sensitivity),
        label = "x"
    )
    val animatedY by animateFloatAsState(
        targetValue = ((rawPitch - centerPitch) * sensitivity),
        label = "y"
    )

    fun center() {
        centerRoll = rawRoll
        centerPitch = rawPitch
    }

    LaunchedEffect(game.adjustCursor) {
        center()
    }

    LaunchedEffect(Unit) {
        center()
    }

    DisposableEffect(Unit) {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            val orientation = FloatArray(3)
            val rotationMatrix = FloatArray(9)

            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)

                    val roll = orientation[2]
                    val pitch = orientation[1]
                    if (!pitch.isNaN() && !roll.isNaN()) {
                        val alpha = 0.1f
                        rawPitch += alpha * (-pitch - rawPitch)
                        rawRoll += alpha * (roll - rawRoll)
                    } else {
                        rawPitch = 0f
                        rawRoll = 0f
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.zIndex(0f)
                .size(50.dp)
                .offset {
                    val maxX = (screenWidthPx - boxSize) / 2
                    val maxY = (screenHeightPx - boxSize) / 2
                    IntOffset(
                        x = animatedX.coerceIn(-maxX, maxX).roundToInt(),
                        y = animatedY.coerceIn(-maxY, maxY).roundToInt()
                    )
                }
        ) {
            Image(
                painter = painterResource(R.drawable.cursor),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .onGloballyPositioned { layoutCoordinates ->
                        game.cursorRect = layoutCoordinates.boundsInWindow()
                    }
            )
        }
    }
}
