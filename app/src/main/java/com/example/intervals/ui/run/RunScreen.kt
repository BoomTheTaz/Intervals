package com.example.intervals.ui.run

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.intervals.timer.IntervalTimerService
import com.example.intervals.timer.Phase
import com.example.intervals.timer.TimerStateHolder
import com.example.intervals.ui.theme.PhaseDone
import com.example.intervals.ui.theme.PhaseRest
import com.example.intervals.ui.theme.PhaseWarmup
import com.example.intervals.ui.theme.PhaseWork
import com.example.intervals.util.formatDuration

@Composable
fun RunScreen(
    presetId: Long,
    onExit: () -> Unit
)
{
    val context = LocalContext.current
    val activity = context as? Activity
    val state by TimerStateHolder.state.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(presetId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        )
        {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (!state.isActive || state.presetId != presetId)
        {
            IntervalTimerService.start(context, presetId)
        }
    }

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    BackHandler {
        IntervalTimerService.stop(context)
        onExit()
    }

    val backgroundColor by animateColorAsState(targetValue = colorFor(state.phase), label = "phaseColor")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .then(
                if (state.phase == Phase.DONE) Modifier.clickable {
                    IntervalTimerService.stop(context)
                    onExit()
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (state.setIndex > 0 && state.totalSets > 0)
            {
                Text(
                    text = "${state.setIndex}/${state.totalSets}",
                    color = Color.White,
                    fontSize = 84.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = formatDuration(state.secondsRemainingInPhase),
                color = Color.White,
                fontSize = 128.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${formatDuration(state.elapsedSeconds)} / ${formatDuration(state.totalSeconds)}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (state.phase == Phase.DONE)
            {
                Text(
                    text = "Tap to exit",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }
}

private fun colorFor(phase: Phase): Color = when (phase)
{
    Phase.WARMUP -> PhaseWarmup
    Phase.WORK -> PhaseWork
    Phase.REST -> PhaseRest
    Phase.COOLDOWN -> PhaseWarmup
    Phase.DONE -> PhaseDone
}
