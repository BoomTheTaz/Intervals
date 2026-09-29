package com.example.intervals.data

import kotlinx.coroutines.flow.Flow

class PresetRepository(private val dao: PresetDao)
{
    val presets: Flow<List<PresetEntity>> = dao.getAll()

    suspend fun getById(id: Long): PresetEntity? = dao.getById(id)

    suspend fun save(preset: PresetEntity): Long =
        if (preset.id == 0L) dao.insert(preset) else { dao.update(preset); preset.id }

    suspend fun delete(preset: PresetEntity) = dao.delete(preset)

    suspend fun markUsed(id: Long) = dao.updateLastUsed(id, System.currentTimeMillis())
}
