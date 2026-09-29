package com.example.intervals.util

fun formatDuration(totalSeconds: Int): String
{
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
