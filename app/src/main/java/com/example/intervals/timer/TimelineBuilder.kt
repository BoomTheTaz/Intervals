package com.example.intervals.timer

import com.example.intervals.data.PresetEntity

enum class Phase { WARMUP, WORK, REST, COOLDOWN, DONE }

data class TimelineStep(
    val phase: Phase,
    val durationSeconds: Int,
    val blockIndex: Int = -1,
    val setIndex: Int = -1,
    val totalSets: Int = -1
)

object TimelineBuilder
{
    fun build(preset: PresetEntity): List<TimelineStep>
    {
        val steps = mutableListOf<TimelineStep>()

        if (preset.warmupSeconds > 0)
        {
            steps += TimelineStep(Phase.WARMUP, preset.warmupSeconds)
        }

        val totalSets = preset.blocks.sumOf { it.repeatCount }
        var overallSetIndex = 0

        preset.blocks.forEachIndexed { blockIndex, block ->
            for (set in 1..block.repeatCount)
            {
                overallSetIndex++
                if (block.workSeconds > 0)
                {
                    steps += TimelineStep(Phase.WORK, block.workSeconds, blockIndex, overallSetIndex, totalSets)
                }
                if (block.restSeconds > 0)
                {
                    steps += TimelineStep(Phase.REST, block.restSeconds, blockIndex, overallSetIndex, totalSets)
                }
            }
        }

        if (preset.cooldownSeconds > 0)
        {
            steps += TimelineStep(Phase.COOLDOWN, preset.cooldownSeconds)
        }

        return steps
    }

    fun totalDurationSeconds(preset: PresetEntity): Int
    {
        val blocksTotal = preset.blocks.sumOf { (it.workSeconds + it.restSeconds) * it.repeatCount }
        return preset.warmupSeconds + blocksTotal + preset.cooldownSeconds
    }
}
