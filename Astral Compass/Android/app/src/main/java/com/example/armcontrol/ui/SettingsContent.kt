package com.example.armcontrol.ui

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.armcontrol.ephemeris.Credentials
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.sample
import kotlin.math.roundToInt

/** State of the "choose a star catalog" field. */
sealed interface StarImportState {
    data object Idle : StarImportState

    /** [progress] is 0..1, or null while the file is still being copied (no size to measure against yet). */
    data class Importing(val fileName: String, val progress: Float?) : StarImportState
    data class Done(val fileName: String, val starCount: Int) : StarImportState
    data class Failed(val fileName: String, val message: String) : StarImportState
}

/**
 * Settings drawer content. Stateless apart from the text being typed: everything that actually
 * happens is a callback, so the ViewModel owns the work.
 *
 * Like the search drawer, this scrolls on its own, so the drawer that hosts it must give it a
 * bounded height (fillMaxSize / fillMaxHeight(fraction) / a fixed height), never wrap-content.
 */
@Composable
fun SettingsContent(
    savedCredentials: Credentials?,
    onSaveCredentials: (Credentials) -> Unit,
    onRemoveCredentials: () -> Unit,
    starImport: StarImportState,
    onImportStarFile: (android.net.Uri) -> Unit,
    onClearCache: () -> Unit,
    brightness: Float,
    onBrightnessSet: (Float) -> Unit,
    serialComm: String,
    frame: ImageBitmap?,
    frameSize: Int,
    currentFrameSize: Int,
    requestFrame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BrightnessControl(brightness, onBrightnessSet)
        HorizontalDivider()
        SpaceTrackSection(savedCredentials, onSaveCredentials, onRemoveCredentials)
        HorizontalDivider()
        StarCatalogSection(starImport, onImportStarFile)
        HorizontalDivider()
        CacheSection(onClearCache)
        HorizontalDivider()
        Text("Logs", style = MaterialTheme.typography.titleMedium)
        Surface (
            color = Color.White,
            modifier = Modifier.height(200.dp).fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(serialComm, modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState()), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        }
        HorizontalDivider()
        FrameDisplay(frame, frameSize, currentFrameSize, requestFrame)
    }
}

@Composable
fun FrameDisplay(frame: ImageBitmap?, frameSize: Int, currentFrameSize: Int, requestFrame: () -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        if(frame == null && frameSize > 0) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).height(100.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LinearProgressIndicator(
                    progress = { currentFrameSize / frameSize.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "${currentFrameSize}/${frameSize}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else if(frame != null) {
            Image(
                bitmap = frame,
                contentDescription = "",
                filterQuality = FilterQuality.None,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth().graphicsLayer {  scaleY = -1f; rotationZ = 90f}
            )
        }
    }
    Column (horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { requestFrame() },
            enabled = frame == null || currentFrameSize == frameSize,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(OutlinedTextFieldDefaults.MinHeight)
        ) {
            if (frame == null || currentFrameSize == frameSize) {
                Text("Request frame")
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current
                )
            }
        }
    }
}

@OptIn(FlowPreview::class)
@Composable
fun BrightnessControl(brightness: Float, onBrightnessSet: (Float) -> Unit, modifier: Modifier = Modifier) {
    val currentOnChange by rememberUpdatedState(onBrightnessSet)
    var local by remember { mutableFloatStateOf(brightness) }
    var dragging by remember { mutableStateOf(false) }

    LaunchedEffect(brightness) { if (!dragging) local = brightness }

    LaunchedEffect(Unit) {
        snapshotFlow { local }.drop(1).sample(50).collect { currentOnChange(it) }
    }

    Text("LED Brightness", style = MaterialTheme.typography.titleMedium)
    Slider(
        value = local,
        onValueChange = { dragging = true; local = it },
        onValueChangeFinished = { dragging = false; currentOnChange(local) },
        modifier = modifier
    )
}

@Composable
private fun SpaceTrackSection(
    saved: Credentials?,
    onSave: (Credentials) -> Unit,
    onRemove: () -> Unit
) {
    // Re-seed the fields if the saved login changes from outside (e.g. it was removed)
    var identity by remember(saved) { mutableStateOf(saved?.identity.orEmpty()) }
    var password by remember(saved) { mutableStateOf(saved?.password.orEmpty()) }
    var showPassword by remember { mutableStateOf(false) }
    val dirty = identity.trim() != saved?.identity.orEmpty() || password != saved?.password.orEmpty()

    Text("Space-Track account", style = MaterialTheme.typography.titleMedium)
    Text(
        "Used to download satellite orbits. Stored encrypted on this device only.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedTextField(
        value = identity,
        onValueChange = { identity = it },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Username (email)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Password") },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            TextButton(onClick = { showPassword = !showPassword }) {
                Text(if (showPassword) "Hide" else "Show")
            }
        }
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Button(
            onClick = { onSave(Credentials(identity.trim(), password)) },
            enabled = identity.isNotBlank() && password.isNotEmpty() && dirty,
            shape = RoundedCornerShape(12.dp)
        ) { Text("Save") }

        if (saved != null) {
            OutlinedButton(onClick = onRemove, shape = RoundedCornerShape(12.dp)) { Text("Remove") }
            if (!dirty) {
                Text(
                    "Saved",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    Text(
        "Applies the next time satellites are refreshed. Clear the cache below to refresh now.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun StarCatalogSection(
    state: StarImportState,
    onPicked: (android.net.Uri) -> Unit
) {
    // "*/*" because CSV files are reported with all sorts of MIME types depending on where they came from
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) onPicked(uri)
    }
    val importing = state is StarImportState.Importing

    Text("Star catalog", style = MaterialTheme.typography.titleMedium)
    Text(
        "Import a HYG database CSV (astronexus/HYGDatabase) to replace the built-in stars.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    val fieldText = when (state) {
        is StarImportState.Idle -> "Built-in catalog"
        is StarImportState.Importing -> state.fileName
        is StarImportState.Done -> state.fileName
        is StarImportState.Failed -> state.fileName
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = fieldText,
            onValueChange = {},
            modifier = Modifier.weight(1f),
            readOnly = true,
            singleLine = true,
            label = { Text("File") },
            isError = state is StarImportState.Failed
        )
        Button(
            onClick = { picker.launch(arrayOf("*/*")) },
            enabled = !importing,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(OutlinedTextFieldDefaults.MinHeight)
        ){
            Text("Choose")
        }
    }

    // Small progress bar directly under the field
    when (state) {
        is StarImportState.Importing -> {
            val p = state.progress
            if (p != null) {
                LinearProgressIndicator(progress = { p }, modifier = Modifier.fillMaxWidth().height(4.dp))
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(4.dp))
            }
            Text(
                if (p != null) "Processing… ${(p * 100).toInt()}%" else "Reading file…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        is StarImportState.Done -> Text(
            "Imported ${state.starCount} stars",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        is StarImportState.Failed -> Text(
            state.message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        is StarImportState.Idle -> Unit
    }
}

@Composable
private fun CacheSection(onClearCache: () -> Unit) {
    var confirming by remember { mutableStateOf(false) }

    Text("Data", style = MaterialTheme.typography.titleMedium)
    Text(
        "Removes the downloaded asteroid, satellite and star data, then downloads it again.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    OutlinedButton(onClick = { confirming = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Text("Clear data cache")
    }
    Spacer(Modifier.height(8.dp))

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text("Clear data cache?") },
            text = { Text("Saved orbits and star positions will be deleted and downloaded again. A star catalog you imported is kept.") },
            confirmButton = {
                TextButton(onClick = { confirming = false; onClearCache() }) { Text("Clear") }
            },
            dismissButton = { TextButton(onClick = { confirming = false }) { Text("Cancel") } }
        )
    }
}
