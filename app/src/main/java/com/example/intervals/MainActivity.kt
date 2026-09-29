package com.example.intervals

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.intervals.navigation.IntervalsNavHost
import com.example.intervals.timer.TimerStateHolder
import com.example.intervals.ui.theme.IntervalsTheme

class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val activeState = TimerStateHolder.state.value
        val startAtRun = if (activeState.isActive) activeState.presetId else null

        setContent {
            IntervalsTheme {
                IntervalsNavHost(startAtRun = startAtRun)
            }
        }
    }
}
