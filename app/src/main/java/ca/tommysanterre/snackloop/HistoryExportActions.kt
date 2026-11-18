package ca.tommysanterre.snackloop

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.time.ZonedDateTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryExportProvider : FileProvider()

@Composable
internal fun HistoryExportButton(state: UiState, onMessage: suspend (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var optionsOpen by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    // Keep the prepared file across recreation while the system's save picker is open.
    var pendingSavePath by rememberSaveable { mutableStateOf<String?>(null) }
    val saveFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        val sourcePath = pendingSavePath
        pendingSavePath = null
        if (uri != null && sourcePath != null) scope.launch {
            exporting = true
            try {
                withContext(Dispatchers.IO) {
                    val output = context.contentResolver.openOutputStream(uri, "wt")
                        ?: error("Couldn't open the selected document")
                    output.use { destination -> File(sourcePath).inputStream().use { it.copyTo(destination) } }
                }
                onMessage("History export saved")
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                onMessage("Couldn't save the export. Try another location.")
            } finally {
                exporting = false
            }
        }
    }

    fun export(share: Boolean) {
        optionsOpen = false
        exporting = true
        val snapshot = state.completions.toList()
        scope.launch {
            try {
                val file = withContext(Dispatchers.IO) { prepareHistoryExport(context, snapshot) }
                if (share) {
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.history-export", file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Hyperbolic Time Chamber workout history")
                        clipData = ClipData.newRawUri("Workout history", uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share workout history"))
                } else {
                    pendingSavePath = file.absolutePath
                    try {
                        saveFile.launch(file.name)
                    } catch (error: Exception) {
                        pendingSavePath = null
                        throw error
                    }
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                onMessage(if (error is ActivityNotFoundException) "No app found for this export action. Try the other option." else "Couldn't prepare the export. Please try again.")
            } finally {
                exporting = false
            }
        }
    }

    TextButton(
        onClick = { optionsOpen = true },
        enabled = state.completions.isNotEmpty() && !state.storageUnavailable && !exporting && pendingSavePath == null
    ) { Text(if (exporting) "Exporting…" else "Export history") }

    if (optionsOpen) AlertDialog(
        onDismissRequest = { optionsOpen = false },
        title = { Text("Export workout history") },
        text = { Text("Create a text file with your current routine, a 28-day summary and all saved movements and skips. Share it in a chat to review exercise difficulty, or save it to attach later.") },
        confirmButton = { TextButton(onClick = { export(share = true) }) { Text("Share file") } },
        dismissButton = { TextButton(onClick = { export(share = false) }) { Text("Save file") } }
    )
}

private fun prepareHistoryExport(context: Context, completions: List<Completion>): File {
    val now = ZonedDateTime.now()
    val directory = File(context.cacheDir, "history-exports")
    check(directory.isDirectory || directory.mkdirs()) { "Couldn't create export directory" }
    return File.createTempFile("Hyperbolic-Time-Chamber-${now.toLocalDate()}-", ".txt", directory).apply {
        writeText(historyExport(completions, now), Charsets.UTF_8)
    }
}
