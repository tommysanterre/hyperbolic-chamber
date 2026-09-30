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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
        setContent { SnackLoopTheme { SnackLoopApp() } }
    }
}

@Composable
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
                        if (historyOpen) "History  ·  ↻ ${state.rotationsToday} today"
                        else "SnackLoop  ·  ↻ ${state.rotationsToday} today"
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
        else ExerciseScreen(exercise, state.completions.isNotEmpty(), vm::complete, vm::undo, Modifier.padding(padding))
    }
}

@Composable
private fun ExerciseScreen(exercise: Exercise, canUndo: Boolean, onComplete: (Int) -> Unit, onUndo: () -> Unit, modifier: Modifier = Modifier) {
    var amount by remember(exercise.id) { mutableStateOf("") }
    val instructionsScrollState = rememberScrollState()
    LaunchedEffect(exercise.id) { instructionsScrollState.scrollTo(0) }
    Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(
            Modifier.weight(1f).verticalScroll(instructionsScrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(exercise.name, style = MaterialTheme.typography.displaySmall)
            Text("Target  ${exercise.target}", style = MaterialTheme.typography.titleMedium)
            MovementDiagram(exercise)
            Text(exercise.description, style = MaterialTheme.typography.bodyLarge)
        }
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter(Char::isDigit) },
            label = { Text(exercise.unit.inputLabel()) },
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
