package com.example.armcontrol.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.armcontrol.ephemeris.*


@Composable
fun CelestialSearchContent(
    objects: List<EphemerisEntry>,
    onTrack: (EphemerisEntry) -> Unit,
    current: EphemerisEntry?,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val typeOptions: List<ObjectType?> = listOf(null) + ObjectType.entries
    var selectedId by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf<ObjectType?>(null) }   // null = All

    val results = remember(objects, query, selectedType) {
        objects.filter { obj ->
            (selectedType == null || obj.type == selectedType) &&
                    obj.name.contains(query.trim(), ignoreCase = true)
        }.sortedBy { obj -> obj.name }

    }
    // Derived from the visible results, so a selection that gets filtered out can't be tracked
    val selected = results.firstOrNull { it.id == selectedId }

    Column(
        modifier = modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search objects") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            }
        )

        LazyRow(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(typeOptions.size) { i ->
                val type = typeOptions[i]
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            role = Role.RadioButton
                        )
                        .padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedType == type, onClick = null)
                    Text(
                        text = type?.label ?: "All",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (results.isEmpty()) {
                Text(
                    "No matching objects",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(results, key = { it.id }) { obj ->
                        val isSelected = obj.id == selectedId || (selectedId == null && obj.id == current?.id)
                        val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                                .selectable(
                                    selected = isSelected,
                                    onClick = { selectedId = if (isSelected) null else obj.id }
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = obj.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = obj.type.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor.copy(alpha = 0.7f)
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 8.dp).size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { selected?.let(onTrack) },
            enabled = selected != null || current != null,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(selected?.let { "${if (current == selected) "Tracking" else "Track"} ${it.name}" } ?: if (current != null) "Tracking ${current.name}" else "Track")
        }
    }
}