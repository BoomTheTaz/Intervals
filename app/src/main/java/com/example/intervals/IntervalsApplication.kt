package com.example.intervals

import android.app.Application
import com.example.intervals.data.IntervalsDatabase
import com.example.intervals.data.PresetRepository

class IntervalsApplication : Application()
{
    val repository: PresetRepository by lazy {
        PresetRepository(IntervalsDatabase.getInstance(this).presetDao())
    }
}
