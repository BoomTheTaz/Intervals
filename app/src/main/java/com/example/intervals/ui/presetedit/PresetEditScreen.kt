package com.example.intervals.ui.presetedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.intervals.IntervalsApplication
import com.example.intervals.data.IntervalBlock
import com.example.intervals.data.PresetEntity
import com.example.intervals.timer.TimelineBuilder
import com.example.intervals.util.formatDuration
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private fun formatMinutes(totalSeconds: Int): String
{
    val minutes = totalSeconds / 60.0
    return if (minutes == minutes.toLong().toDouble()) minutes.toLong().toString() else minutes.toString()
}

private class EditableBlock(val key: Int, work: Int, rest: Int, repeat: Int)
{
    var work by mutableStateOf(work.toString())
    var rest by mutableStateOf(rest.toString())
    var repeat by mutableStateOf(repeat.toString())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetEditScreen(
    presetId: Long,
    onDone: () -> Unit
)
{
    val repository = (LocalContext.current.applicationContext as IntervalsApplication).repository
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var warmupMinutes by remember { mutableStateOf("3") }
    var cooldownMinutes by remember { mutableStateOf("2") }
    var nextKey by remember { mutableIntStateOf(0) }
    val blocks = remember { mutableStateListOf<EditableBlock>() }
    var loaded by remember { mutableStateOf(presetId == 0L) }
    var lastUsedAt by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(presetId) {
        if (presetId != 0L)
        {
            repository.getById(presetId)?.let { preset ->
                name = preset.name
                warmupMinutes = formatMinutes(preset.warmupSeconds)
                cooldownMinutes = formatMinutes(preset.cooldownSeconds)
                lastUsedAt = preset.lastUsedAt
                preset.blocks.forEach { block ->
                    blocks.add(EditableBlock(nextKey++, block.workSeconds, block.restSeconds, block.repeatCount))
                }
            }
            loaded = true
        }
        else if (blocks.isEmpty())
        {
            blocks.add(EditableBlock(nextKey++, 30, 60, 10))
        }
    }

    fun currentPreset() = PresetEntity(
        id = presetId,
        name = name.ifBlank { "Workout" },
        warmupSeconds = ((warmupMinutes.toDoubleOrNull() ?: 0.0) * 60).roundToInt(),
        cooldownSeconds = ((cooldownMinutes.toDoubleOrNull() ?: 0.0) * 60).roundToInt(),
        lastUsedAt = lastUsedAt,
        blocks = blocks.map {
            IntervalBlock(
                workSeconds = it.work.toIntOrNull() ?: 0,
                restSeconds = it.rest.toIntOrNull() ?: 0,
                repeatCount = it.repeat.toIntOrNull() ?: 1
            )
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (presetId == 0L) "New preset" else "Edit preset") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (!loaded) return@Scaffold

        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = warmupMinutes,
                    onValueChange = { warmupMinutes = it },
                    label = { Text("Warmup (min)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = cooldownMinutes,
                    onValueChange = { cooldownMinutes = it },
                    label = { Text("Cooldown (min)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Total time: ${formatDuration(TimelineBuilder.totalDurationSeconds(currentPreset()))}",
                modifier = Modifier.padding(top = 12.dp)
            )

            Text(text = "Interval blocks", modifier = Modifier.padding(top = 20.dp, bottom = 4.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(blocks, key = { it.key }) { block ->
                    BlockRow(block = block, onDelete = { blocks.remove(block) })
                }
            }

            Button(
                onClick = { blocks.add(EditableBlock(nextKey++, 30, 60, 10)) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Add block")
            }

            Button(
                onClick = {
                    scope.launch {
                        repository.save(currentPreset())
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun BlockRow(block: EditableBlock, onDelete: () -> Unit)
{
    val focusManager = LocalFocusManager.current

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = block.work,
                onValueChange = { block.work = it },
                label = { Text("Work (s)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = block.rest,
                onValueChange = { block.rest = it },
                label = { Text("Rest (s)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = block.repeat,
                onValueChange = { block.repeat = it },
                label = { Text("Sets") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remove block")
            }
        }
    }
}
