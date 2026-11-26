package ca.tommysanterre.snackloop

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HyperbolicTheme { HyperbolicApp() } }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HyperbolicApp(vm: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val exercise = RoutineCatalog.exerciseAt(state.currentIndex)
    LaunchedEffect(context, snackbarHostState) {
        val update = checkForUpdate(context) ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "New version ${update.version} available",
            actionLabel = "View release",
            withDismissAction = true,
            duration = SnackbarDuration.Indefinite
        )
        if (result == SnackbarResult.ActionPerformed) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(update.releaseUrl)))
            } catch (_: ActivityNotFoundException) {
                snackbarHostState.showSnackbar("No browser found to open the release")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                        .heightIn(min = 80.dp)
                        .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Hyperbolic Time Chamber",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp)
                        )
                        Text(
                            "\u21BB ${state.rotationsToday} today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(
                        onClick = {
                            val report = historyExport(state.completions)
                            val clipboard = context.getSystemService(ClipboardManager::class.java)
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Workout history", report))
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(
                                    if (clipboard != null) "History copied" else "Clipboard unavailable"
                                )
                            }
                        },
                        enabled = state.completions.isNotEmpty() && !state.storageUnavailable
                    ) { Text("Copy history") }
                }
            }
        }
    ) { padding ->
        ExerciseScreen(
            exercise = exercise,
            superSaiyan = state.superSaiyanUnlocked,
            canUndo = state.completions.isNotEmpty() && !state.storageUnavailable,
            storageUnavailable = state.storageUnavailable,
            onComplete = vm::complete,
            onUndo = vm::undo,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun ExerciseScreen(
    exercise: Exercise,
    superSaiyan: Boolean,
    canUndo: Boolean,
    storageUnavailable: Boolean,
    onComplete: (Int) -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var submitting by remember(exercise.id) { mutableStateOf(false) }
    val amounts = exercise.recordingAmounts()
    val instructionsScrollState = rememberScrollState()
    LaunchedEffect(exercise.id) { instructionsScrollState.scrollTo(0) }
    Column(modifier.fillMaxSize().padding(24.dp)) {
        Column(
            Modifier.fillMaxWidth().weight(1f).verticalScroll(instructionsScrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(exercise.name, style = MaterialTheme.typography.displaySmall)
                Text("Target  ${exercise.target}", style = MaterialTheme.typography.titleMedium)
                MovementDiagram(exercise, superSaiyan)
                Text(exercise.description, style = MaterialTheme.typography.bodyLarge)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(exercise.unit.inputLabel(), style = MaterialTheme.typography.titleMedium)
                Text("Tap a number to record and move on, or 0 to skip", style = MaterialTheme.typography.bodyMedium)
            }
            amounts.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { amount ->
                        FilledTonalButton(
                            onClick = {
                                if (!submitting && !storageUnavailable) {
                                    submitting = true
                                    onComplete(amount)
                                }
                            },
                            enabled = !storageUnavailable && !submitting,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp).semantics {
                                contentDescription = exercise.recordingActionDescription(amount)
                            }
                        ) {
                            Text(amount.toString(), style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            if (storageUnavailable) {
                Text(
                    "Your saved history couldn't be opened. Hyperbolic Time Chamber will stay open, but recording is unavailable. Try restarting the app.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        TextButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Undo last") }
    }
}
