package com.example.intervals.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object TimerStateHolder
{
    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state

    fun update(state: TimerState)
    {
        _state.value = state
    }

    fun reset()
    {
        _state.value = TimerState()
    }
}
