package ca.tommysanterre.snackloop

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SnackLoopTheme { SnackLoopApp() } }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SnackLoopApp(vm: MainViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var historyOpen by remember { mutableStateOf(false) }
    val exercise = exercises[state.currentIndex]
    BackHandler(enabled = historyOpen) { historyOpen = false }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (historyOpen) "History  \u00B7  \u21BB ${state.rotationsToday} today"
                        else "SnackLoop  \u00B7  \u21BB ${state.rotationsToday} today"
                    )
                },
                navigationIcon = {
                    if (historyOpen) IconButton(onClick = { historyOpen = false }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!historyOpen) IconButton(onClick = { historyOpen = true }) {
                        Icon(Icons.Default.History, "History")
                    }
                }
            )
        }
    ) { padding ->
        if (historyOpen) HistoryScreen(state, Modifier.padding(padding))
        else ExerciseScreen(
            exercise = exercise,
            superSaiyan = state.completedRotations > 1,
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
    val amounts = when (exercise.id) {
        "pushups" -> (5..10).toList()
        "squats" -> (10..15).toList()
        "pullups" -> (2..5).toList()
        "reverse_lunges" -> listOf(5)
        else -> (5..30 step 5).toList()
    }
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
                Text("Tap a number to record and move on", style = MaterialTheme.typography.bodyMedium)
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
                                contentDescription = "Record $amount ${exercise.unit.historyLabel(amount)} and go to next exercise"
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
                    "Your saved history couldn't be opened. SnackLoop will stay open, but recording is unavailable. Try restarting the app.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        TextButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Undo last") }
    }
}

@Composable
private fun HistoryScreen(state: UiState, modifier: Modifier = Modifier) {
    if (state.completions.isEmpty()) Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No movement snacks yet") }
    else LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(state.completions, key = { it.id }) { item ->
            val ex = exercises.firstOrNull { it.id == item.exerciseId }
            ListItem(
                headlineContent = { Text(ex?.name ?: item.exerciseId) },
                supportingContent = { Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(item.completedAt))) },
                trailingContent = { Text("${item.amount} ${ex?.unit?.historyLabel(item.amount) ?: "units"}") }
            )
        }
    }
}
