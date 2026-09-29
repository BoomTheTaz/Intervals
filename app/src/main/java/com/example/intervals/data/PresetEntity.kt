package com.example.intervals.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

data class IntervalBlock(
    val workSeconds: Int,
    val restSeconds: Int,
    val repeatCount: Int
)

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val warmupSeconds: Int,
    val cooldownSeconds: Int,
    val blocks: List<IntervalBlock>,
    val lastUsedAt: Long = System.currentTimeMillis()
)

class Converters
{
    @TypeConverter
    fun fromBlocks(blocks: List<IntervalBlock>): String =
        blocks.joinToString("|") { "${it.workSeconds},${it.restSeconds},${it.repeatCount}" }

    @TypeConverter
    fun toBlocks(raw: String): List<IntervalBlock>
    {
        if (raw.isEmpty()) return emptyList()
        return raw.split("|").map { entry ->
            val (work, rest, repeat) = entry.split(",").map { it.toInt() }
            IntervalBlock(work, rest, repeat)
        }
    }
}
