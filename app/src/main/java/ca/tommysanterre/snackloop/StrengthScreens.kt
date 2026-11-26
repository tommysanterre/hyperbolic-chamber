package ca.tommysanterre.snackloop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun StrengthTabScreen(
    state: UiState,
    onStartSession: (String) -> Unit,
    onRecordSet: (Int, String) -> Unit,
    onUndoSet: () -> Unit,
    onAbandonSession: () -> Unit,
    onFinishSession: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val active = state.activeStrength
    if (active == null) {
        StrengthLauncherScreen(
            recommendedTemplate = state.recommendedTemplate,
            lastPerformance = state.lastPerformance,
            completedSessions = state.completedStrengthSessions,
            storageUnavailable = state.storageUnavailable,
            onStartSession = onStartSession,
            modifier = modifier
        )
    } else {
        ActiveStrengthScreen(
            active = active,
            superSaiyan = state.superSaiyanUnlocked,
            lastPerformance = state.lastPerformance,
            storageUnavailable = state.storageUnavailable,
            onRecordSet = onRecordSet,
            onUndoSet = onUndoSet,
            onAbandonSession = onAbandonSession,
            onFinishSession = onFinishSession,
            modifier = modifier
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun StrengthLauncherScreen(
    recommendedTemplate: StrengthTemplate,
    lastPerformance: Map<String, ExerciseLastPerformance>,
    completedSessions: Int,
    storageUnavailable: Boolean,
    onStartSession: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTemplateId by rememberSaveable(recommendedTemplate.id) {
        mutableStateOf(recommendedTemplate.id)
    }
    val selected = StrengthCatalog.templateForId(selectedTemplateId) ?: recommendedTemplate
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("30-Minute Full-Body Strength", style = MaterialTheme.typography.headlineSmall)
            Text(
                if (completedSessions == 0) "Alternate Strength A and Strength B (2×/week). Stop with 1–2 clean reps in reserve."
                else "$completedSessions completed · Next recommended: ${recommendedTemplate.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StrengthCatalog.templates.forEach { template ->
                val isSelected = template.id == selected.id
                val isRecommended = template.id == recommendedTemplate.id
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTemplateId = template.id },
                    label = {
                        Text(
                            if (isRecommended) "${template.name} (Next)" else template.name,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Button(
            onClick = { onStartSession(selected.id) },
            enabled = !storageUnavailable,
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Text(
                "Start ${selected.name} (${selected.totalWorkingSets} sets)",
                style = MaterialTheme.typography.titleMedium
            )
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "${selected.name} · ${selected.subtitle}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                selected.pairs.forEach { pair ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "${pair.label} · ${pair.rounds} rounds (${pair.restAfterFirstSeconds}s / ${pair.restAfterSecondSeconds}s rest)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        listOf(pair.first, pair.second).forEach { exercise ->
                            val perf = lastPerformance[exercise.id]
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${exercise.slotLabel}  ${exercise.name}",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        exercise.target,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (perf != null) {
                                    val repsSummary = perf.amounts.joinToString(", ") {
                                        if (it == 0) "skip" else it.toString()
                                    }
                                    val hint = if (perf.readyToProgress && perf.suggestedNextSetting != null) {
                                        " · Ready → ${perf.suggestedNextSetting}"
                                    } else ""
                                    Text(
                                        "Last (${perf.setting}): $repsSummary$hint",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (perf.readyToProgress) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (storageUnavailable) {
            Text(
                "Your saved history couldn't be opened. Strength tracking is unavailable until storage recovers.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
private fun ActiveStrengthScreen(
    active: ActiveStrengthSessionState,
    superSaiyan: Boolean,
    lastPerformance: Map<String, ExerciseLastPerformance>,
    storageUnavailable: Boolean,
    onRecordSet: (Int, String) -> Unit,
    onUndoSet: () -> Unit,
    onAbandonSession: () -> Unit,
    onFinishSession: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var warmupDismissedForId by rememberSaveable { mutableLongStateOf(-1L) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val showWarmup = active.logs.isEmpty() && warmupDismissedForId != active.session.id

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard ${active.template.name}?") },
            text = { Text("This removes the in-progress session and its logged sets.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onAbandonSession()
                    }
                ) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep going") }
            }
        )
    }

    when {
        showWarmup -> {
            WarmupCardScreen(
                template = active.template,
                onBegin = { warmupDismissedForId = active.session.id },
                onDiscard = { showDiscardDialog = true },
                modifier = modifier
            )
        }
        active.isReadyToFinish -> {
            CooldownFinishScreen(
                active = active,
                storageUnavailable = storageUnavailable,
                onUndoSet = onUndoSet,
                onFinish = onFinishSession,
                onDiscard = { showDiscardDialog = true },
                modifier = modifier
            )
        }
        else -> {
            val step = active.currentStep ?: return
            val exercise = step.exercise
            val scrollState = rememberScrollState()
            var submitting by remember(active.session.id, step.stepIndex) { mutableStateOf(false) }
            val initialSetting = remember(active.session.id, step.stepIndex, exercise.id) {
                defaultSettingForExercise(exercise, active.logs, lastPerformance)
            }
            var selectedSetting by remember(active.session.id, step.stepIndex, initialSetting) {
                mutableStateOf(initialSetting)
            }
            LaunchedEffect(active.session.id, step.stepIndex) { scrollState.scrollTo(0) }

            val perf = lastPerformance[exercise.id]
            val amounts = exercise.recordingAmounts()

            Column(modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 12.dp)) {
                // Header progress + visual rest timer
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${active.template.name} · ${step.pairLabel} · Set ${step.setNumber}/${step.totalSets}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "${step.stepIndex + 1} / ${active.steps.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (step.stepIndex.toFloat() / active.steps.size.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (active.lastCompletedAt != null && active.lastRestSeconds != null) {
                        VisualRestTimerBanner(
                            restStartedAtMs = active.lastCompletedAt,
                            restDurationSeconds = active.lastRestSeconds
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "${exercise.slotLabel} · ${exercise.name}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text("Target  ${exercise.target}", style = MaterialTheme.typography.titleMedium)
                        if (perf != null) {
                            val lastLine = perf.amounts.joinToString(", ") { if (it == 0) "skip" else it.toString() }
                            Text(
                                "Last time (${perf.setting}): $lastLine",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (perf.readyToProgress && perf.suggestedNextSetting != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        "Hit top of range last session — try ${perf.suggestedNextSetting}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (exercise.artwork != null) {
                        MovementDiagram(exercise.name, exercise.artwork, superSaiyan)
                    } else {
                        PendingArtworkCard(exercise = exercise, selectedSetting = selectedSetting)
                    }

                    // Progression / setting chips (pre-selected for zero extra taps on normal sets)
                    if (exercise.settings.size > 1) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Setup / Progression",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                exercise.settings.forEach { setting ->
                                    FilterChip(
                                        selected = selectedSetting == setting,
                                        onClick = { selectedSetting = setting },
                                        label = { Text(setting) }
                                    )
                                }
                            }
                        }
                    }

                    Text(exercise.description, style = MaterialTheme.typography.bodyLarge)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(exercise.unit.inputLabel(), style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Tap a number to record Set ${step.setNumber}/${step.totalSets}, or 0 to skip",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    amounts.chunked(4).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { amount ->
                                FilledTonalButton(
                                    onClick = {
                                        if (!submitting && !storageUnavailable) {
                                            submitting = true
                                            onRecordSet(amount, selectedSetting)
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
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onUndoSet,
                        enabled = active.logs.isNotEmpty() && !storageUnavailable
                    ) { Text("Undo last") }
                    TextButton(
                        onClick = { showDiscardDialog = true },
                        enabled = !storageUnavailable
                    ) { Text("Discard") }
                }
            }
        }
    }
}

@Composable
private fun VisualRestTimerBanner(
    restStartedAtMs: Long,
    restDurationSeconds: Int
) {
    var nowMs by remember(restStartedAtMs) { mutableLongStateOf(System.currentTimeMillis()) }
    val endMs = restStartedAtMs + restDurationSeconds * 1000L
    val remainingMs = (endMs - nowMs).coerceAtLeast(0L)

    LaunchedEffect(restStartedAtMs, restDurationSeconds) {
        while (System.currentTimeMillis() < endMs) {
            nowMs = System.currentTimeMillis()
            delay(200)
        }
        nowMs = endMs
    }

    val remainingSeconds = ((remainingMs + 999L) / 1000L).toInt()
    val isDone = remainingMs <= 0L
    val fraction = if (restDurationSeconds > 0) {
        (remainingMs.toFloat() / (restDurationSeconds * 1000f)).coerceIn(0f, 1f)
    } else 0f

    Surface(
        color = if (isDone) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isDone) "Rest complete — ready when you are" else "Rest timer",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isDone) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (isDone) "0:00" else formatSeconds(remainingSeconds),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDone) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.primary
                )
            }
            if (!isDone) {
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PendingArtworkCard(
    exercise: StrengthExercise,
    selectedSetting: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "${exercise.slotLabel} · $selectedSetting",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                exercise.name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                "Custom DBZ form artwork pending generation — follow the setup cue below.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WarmupCardScreen(
    template: StrengthTemplate,
    onBegin: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("${template.name} — Warm-up (4 min)", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Move smoothly to warm up joints and groove your first pair before starting the clock.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                template.warmupItems.forEach { item ->
                    Text("•  $item", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onBegin,
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Text("Begin Pair A (${template.totalWorkingSets} sets)", style = MaterialTheme.typography.titleMedium)
        }
        OutlinedButton(
            onClick = onDiscard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}

@Composable
private fun CooldownFinishScreen(
    active: ActiveStrengthSessionState,
    storageUnavailable: Boolean,
    onUndoSet: () -> Unit,
    onFinish: (String) -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var note by rememberSaveable(active.session.id) { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val elapsedMinutes = max(1L, (System.currentTimeMillis() - active.session.startedAt + 30_000L) / 60_000L)
    val logsByExercise = active.logs.groupBy { it.exerciseId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("${active.template.name} — Cooldown (2 min)", style = MaterialTheme.typography.headlineSmall)
        Text(
            "${active.completedSetsCount} of ${active.steps.size} working sets logged · ~$elapsedMinutes min elapsed",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Cooldown", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                active.template.cooldownItems.forEach { item ->
                    Text("•  $item", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Session summary", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                active.template.exercises.forEach { exercise ->
                    val entries = logsByExercise[exercise.id].orEmpty()
                    val setting = entries.lastOrNull { it.setting.isNotBlank() }?.setting ?: exercise.defaultSetting
                    val values = entries.joinToString(", ") { if (it.amount == 0) "skip" else it.amount.toString() }
                    Text(
                        "${exercise.slotLabel} ${exercise.name} ($setting): $values",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Optional note (feel, RIR, joint check)") },
            singleLine = false,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onFinish(note.trim()) },
            enabled = !storageUnavailable,
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Text("Finish & Transform", style = MaterialTheme.typography.titleMedium)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onUndoSet, enabled = active.logs.isNotEmpty() && !storageUnavailable) {
                Text("Undo last set")
            }
            TextButton(onClick = onDiscard, enabled = !storageUnavailable) {
                Text("Discard")
            }
        }
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val clamped = totalSeconds.coerceAtLeast(0)
    val mins = clamped / 60
    val secs = clamped % 60
    return "$mins:${secs.toString().padStart(2, '0')}"
}
