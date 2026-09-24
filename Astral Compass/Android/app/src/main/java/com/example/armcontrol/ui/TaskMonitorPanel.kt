package com.example.armcontrol.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.armcontrol.models.SystemState
import com.example.armcontrol.models.TaskState
import java.util.Locale
import kotlin.time.Duration.Companion.microseconds

private data class Trace(
    val label: String,
    val color: Color,
    val values: List<Float>,
    val format: (Float) -> String
)

private val coreColors = listOf(
    Color(0xFFE53935), Color(0xFF43A047), Color(0xFFFB8C00), Color(0xFF8E24AA)
)

@Composable
fun TaskMonitorPanel(history: List<SystemState>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        SystemGraph(
            history = history,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        )
        Spacer(Modifier.height(12.dp))
        TaskList(
            tasks = history.lastOrNull()?.taskStates?.values?.toList() ?: emptyList()
        )
    }
}


@Composable
fun SystemGraph(history: List<SystemState>, modifier: Modifier = Modifier) {
    if (history.size < 2) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text("No data yet", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    val heapValues = history.map { it.heapUsage * 100f }
    val heapTrace = remember(history) {
        Trace("Heap Used", Color(0xFF1E88E5), heapValues) { String.format(Locale.US, "%.1f%%", it) }
    }

    val coreIds = remember(history) {
        history.flatMap { it.coreIDLE.keys }.distinct().sorted()
    }

    val coreTraces = remember(history, coreIds) {
        coreIds.mapIndexed { index, coreId ->
            val usageValues = history.map { state ->
                val idle = state.coreIDLE[coreId] ?: 1f
                ((1f - idle) * 100f).coerceIn(0f, 100f)
            }
            Trace("Core $coreId", coreColors[index % coreColors.size], usageValues) {
                String.format(Locale.US, "%.1f%%", it)
            }
        }
    }

    val current = history.last()
    val utilization = current.taskStates.values.sumOf { it.utilization.toDouble() }.toFloat() * 100f / coreIds.size
    val clockSpeed = current.clockSpeed
    val taskCount = current.taskStates.size
    val upTime = history.last().totalTime.microseconds.toComponents { hours, minutes, seconds, _ ->
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TraceGraph(
            trace = heapTrace,
            modifier = Modifier.weight(1f).fillMaxWidth()
        )

        CoreUsageGrid(
            coreTraces = coreTraces,
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Util: ${String.format(Locale.US, "%.1f%%", utilization)}", style = MaterialTheme.typography.labelSmall)
            Text("Speed: ${clockSpeed}MHz", style = MaterialTheme.typography.labelSmall)
            Text("Tasks: $taskCount", style = MaterialTheme.typography.labelSmall)
            Text("Up Time: $upTime", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun CoreUsageGrid(coreTraces: List<Trace>, modifier: Modifier = Modifier) {
    if (coreTraces.isEmpty()) return

    val columns = when {
        coreTraces.size <= 1 -> 1
        coreTraces.size <= 4 -> 2
        else -> 3
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        coreTraces.chunked(columns).forEach { rowTraces ->
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowTraces.forEach { trace ->
                    TraceGraph(trace = trace, modifier = Modifier.weight(1f).fillMaxHeight())
                }
                // pad the last row so a partial row doesn't stretch to fill the width
                repeat(columns - rowTraces.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TraceGraph(trace: Trace, modifier: Modifier = Modifier) {
    val current = trace.values.last()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(trace.color, CircleShape))
                Spacer(Modifier.width(4.dp))
                Text(trace.label, style = MaterialTheme.typography.labelSmall)
            }
            Text(trace.format(current), style = MaterialTheme.typography.labelSmall)
        }

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.25f),
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = 1.5f
                )
                drawTrace(trace.values, trace.color, maxValue = 100f)
            }
            Text("100%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.Gray, modifier = Modifier.align(Alignment.TopStart))
            Text("0%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.Gray, modifier = Modifier.align(Alignment.BottomStart))
        }
    }
}

private fun DrawScope.drawTrace(values: List<Float>, color: Color, maxValue: Float = 100f) {
    if (values.size < 2) return

    val stepX = size.width / (values.size - 1)
    val path = Path()

    values.forEachIndexed { index, v ->
        val x = index * stepX
        val normalized = (v / maxValue).coerceIn(0f, 1f)
        val y = size.height - (normalized * size.height * 0.85f) - size.height * 0.075f
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }

    drawPath(path, color = color, style = Stroke(width = 3f, cap = StrokeCap.Round))
}

@Composable
private fun TaskList(tasks: List<TaskState>, modifier: Modifier = Modifier) {
    val mono = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Text("Task", style = mono, modifier = Modifier.weight(1.6f))
            Text("CPU%", style = mono, modifier = Modifier.weight(0.8f))
            Text("Pri", style = mono, modifier = Modifier.weight(0.5f))
            Text("St", style = mono, modifier = Modifier.weight(0.4f))
            Text("Stack", style = mono, modifier = Modifier.weight(0.9f))
        }
        HorizontalDivider()

        Column(
            modifier = Modifier
                .heightIn(max = 220.dp)
                .verticalScroll(rememberScrollState())
        ) {
            tasks.sortedByDescending { it.utilization }.forEach { task ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Text(task.name, style = mono, modifier = Modifier.weight(1.6f), maxLines = 1)
                    Text(
                        String.format(Locale.US, "%.1f", task.utilization * 100),
                        style = mono,
                        modifier = Modifier.weight(0.8f)
                    )
                    Text("${task.priority}", style = mono, modifier = Modifier.weight(0.5f))
                    Text(taskStateLabel(task.state), style = mono, modifier = Modifier.weight(0.4f))
                    Text("${task.stackHighWaterMark}", style = mono, modifier = Modifier.weight(0.9f))
                }
            }
        }
    }
}

// Matches FreeRTOS's eTaskState enum ordering: 0=Running, 1=Ready, 2=Blocked, 3=Suspended, 4=Deleted
private fun taskStateLabel(state: Int): String = when (state) {
    0 -> "Rn"
    1 -> "Rd"
    2 -> "Bl"
    3 -> "Sp"
    4 -> "Dl"
    else -> "?"
}