package com.example.intervals.timer

data class TimerState(
    val isActive: Boolean = false,
    val presetId: Long = -1,
    val presetName: String = "",
    val phase: Phase = Phase.DONE,
    val secondsRemainingInPhase: Int = 0,
    val setIndex: Int = -1,
    val totalSets: Int = -1,
    val elapsedSeconds: Int = 0,
    val totalSeconds: Int = 0
)
