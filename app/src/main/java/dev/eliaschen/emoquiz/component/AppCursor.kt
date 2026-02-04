package dev.eliaschen.emoquiz.component

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.eliaschen.emoquiz.LocalGameDataViewModel
import dev.eliaschen.emoquiz.R
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
                        rawPitch = -pitch
                        rawRoll = roll
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
            modifier = Modifier
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
                    .size(15.dp)
                    .onGloballyPositioned { layoutCoordinates ->
                        game.cursorRect = layoutCoordinates.boundsInWindow()
                    }
            )
        }
        IconButton(
            onClick = {
                center()
            },
            modifier = Modifier
                .padding(100.dp)
                .align(Alignment.BottomCenter)
        ) {
            Icon(painter = painterResource(R.drawable.adjust), contentDescription = null)
        }
    }
}
