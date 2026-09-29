package com.example.intervals.ui.presetlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.intervals.IntervalsApplication
import com.example.intervals.data.PresetEntity
import com.example.intervals.timer.TimelineBuilder
import com.example.intervals.util.formatDuration
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetListScreen(
    onAddPreset: () -> Unit,
    onEditPreset: (Long) -> Unit,
    onRunPreset: (Long) -> Unit
)
{
    val repository = (LocalContext.current.applicationContext as IntervalsApplication).repository
    val presets by repository.presets.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var presetPendingDelete by remember { mutableStateOf<PresetEntity?>(null) }

    presetPendingDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetPendingDelete = null },
            title = { Text("Delete preset?") },
            text = { Text("\"${preset.name}\" will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repository.delete(preset) }
                    presetPendingDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { presetPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Interval presets") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPreset) {
                Icon(Icons.Default.Add, contentDescription = "New preset")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(presets, key = { it.id }) { preset ->
                PresetRow(
                    preset = preset,
                    onClick = { onEditPreset(preset.id) },
                    onPlay = { onRunPreset(preset.id) },
                    onDuplicate = {
                        scope.launch {
                            repository.save(
                                preset.copy(id = 0L, name = "${preset.name} copy", lastUsedAt = System.currentTimeMillis())
                            )
                        }
                    },
                    onDelete = { presetPendingDelete = preset }
                )
            }
        }
    }
}

@Composable
private fun PresetRow(
    preset: PresetEntity,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
)
{
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clickable(onClick = onClick)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = preset.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDuration(TimelineBuilder.totalDurationSeconds(preset)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(text = summarize(preset), style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick = onPlay) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDuplicate) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate")
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

private fun summarize(preset: PresetEntity): String
{
    val warmup = "${formatMinutes(preset.warmupSeconds)}min warmup"
    val cooldown = "${formatMinutes(preset.cooldownSeconds)}min cooldown"
    val blocks = preset.blocks.joinToString(" + ") { "${it.repeatCount}x${it.workSeconds}/${it.restSeconds}" }
    return "$warmup · $blocks · $cooldown"
}

private fun formatMinutes(totalSeconds: Int): String
{
    val minutes = totalSeconds / 60.0
    return if (minutes == minutes.toLong().toDouble()) minutes.toLong().toString() else minutes.toString()
}
