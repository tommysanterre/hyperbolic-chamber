package ca.tommysanterre.snackloop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MaterialTheme { SnackLoopApp() } }
    }
}

@Composable
fun SnackLoopApp(vm: MainViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var historyOpen by remember { mutableStateOf(false) }
    val exercise = exercises[state.currentIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (historyOpen) "History" else "SnackLoop  ·  ↻ ${state.rotationsToday} today") },
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
        else ExerciseScreen(exercise, state.completions.isNotEmpty(), vm::complete, vm::undo, Modifier.padding(padding))
    }
}

@Composable
private fun ExerciseScreen(exercise: Exercise, canUndo: Boolean, onComplete: (Int) -> Unit, onUndo: () -> Unit, modifier: Modifier = Modifier) {
    var amount by remember(exercise.id) { mutableStateOf("") }
    Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(exercise.name, style = MaterialTheme.typography.displaySmall)
        Text("Target  ${exercise.target}", style = MaterialTheme.typography.titleMedium)
        MovementDiagram(exercise)
        Text(exercise.description, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.weight(1f))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter(Char::isDigit) },
            label = { Text(unitLabel(exercise.unit)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { amount.toIntOrNull()?.let { onComplete(it); amount = "" } },
            enabled = (amount.toIntOrNull() ?: 0) > 0,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text("Complete") }
        TextButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Undo last") }
    }
}

private fun unitLabel(unit: UnitType) = when (unit) {
    UnitType.REPS -> "Reps completed"
    UnitType.SECONDS -> "Seconds held"
    UnitType.REPS_PER_SIDE -> "Reps per side"
    UnitType.SECONDS_PER_SIDE -> "Seconds per side"
}

@Composable
private fun MovementDiagram(exercise: Exercise) {
    // Deliberately simple first-pass start/end cards. These are replaced by illustrated assets next.
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ElevatedCard(Modifier.weight(1f).height(150.dp)) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("START\n${exercise.name}") } }
        ElevatedCard(Modifier.weight(1f).height(150.dp)) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("END\n${exercise.name}") } }
    }
}

@Composable
private fun HistoryScreen(state: UiState, modifier: Modifier = Modifier) {
    if (state.completions.isEmpty()) Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No movement snacks yet") }
    else LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(state.completions, key = { it.id }) { item ->
            val ex = exercises.first { it.id == item.exerciseId }
            ListItem(
                headlineContent = { Text(ex.name) },
                supportingContent = { Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(item.completedAt))) },
                trailingContent = { Text("${item.amount} ${if (ex.unit == UnitType.SECONDS || ex.unit == UnitType.SECONDS_PER_SIDE) "sec" else "reps"}") }
            )
        }
    }
}
