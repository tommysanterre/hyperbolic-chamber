package ca.tommysanterre.snackloop

import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

/** A portable text report: strength sessions, recent snack amounts, and complete saved logs. */
internal fun historyExport(
    completions: List<Completion>,
    generatedAt: ZonedDateTime = ZonedDateTime.now(),
    strengthSessions: List<StrengthSession> = emptyList(),
    strengthSetLogs: List<StrengthSetLog> = emptyList()
): String = buildString {
    val zone = generatedAt.zone
    val today = workoutDate(generatedAt)
    val start = today.minusDays(27)
    val ordered = completions.sortedWith(compareBy<Completion> { it.completedAt }.thenBy { it.id })
    fun Long.workoutDay() = workoutDate(Instant.ofEpochMilli(this).atZone(zone))
    fun Completion.day() = completedAt.workoutDay()
    val recent = ordered.filter { it.day() in start..today }
    val rotation = rotationState(ordered, today, zone)
    val finishedStrength = strengthSessions
        .filter { it.finishedAt != null }
        .sortedWith(compareBy<StrengthSession> { it.startedAt }.thenBy { it.id })
    val logsBySession = strengthSetLogs
        .sortedWith(compareBy<StrengthSetLog> { it.stepIndex }.thenBy { it.id })
        .groupBy { it.sessionId }

    val exercises = RoutineCatalog.exercises

    appendLine("# Hyperbolic Time Chamber — workout history")
    appendLine()
    appendLine("Exported: ${generatedAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}")
    appendLine("Time zone: $zone. Workout days run from 4 AM to 4 AM in this zone.")
    appendLine("Current routine: ${RoutineCatalog.VERSION}. Saved records: ${ordered.size}.")
    appendLine("Completed loops (all time): ${rotation.completedRotations}. Completed strength sessions (all time): ${finishedStrength.size}.")
    appendLine()
    appendLine("## Context to add when asking about difficulty")
    appendLine()
    appendLine("Share how hard each movement felt, how many more clean reps or seconds you could manage, and any assistance, setup changes or discomfort. These details are not recorded by the app.")
    appendLine("Amounts are self-reported. A skip does not explain why a movement was skipped. Reaching the highest recording button does not by itself mean a movement is ready to progress.")
    appendLine("Per-side amounts are recorded per side, not combined. Undone entries are no longer in the saved log.")
    appendLine()
    if (finishedStrength.isNotEmpty()) {
        appendLine("## Strength sessions (oldest first)")
        appendLine()
        finishedStrength.forEach { session ->
            val template = StrengthCatalog.templateForId(session.templateId)
            val sessionDate = (session.finishedAt ?: session.startedAt).workoutDay()
            val durationMin = max(1L, ((session.finishedAt ?: session.startedAt) - session.startedAt + 30_000L) / 60_000L)
            appendLine("### $sessionDate — ${(template?.name ?: session.templateId).cell()} (~$durationMin min)")
            val sessionLogs = logsBySession[session.id].orEmpty()
            val byExercise = sessionLogs.groupBy { it.exerciseId }
            val orderedIds = template?.exercises?.map { it.id }
                ?.plus(byExercise.keys.filter { id -> template.exercises.none { it.id == id } })
                ?: byExercise.keys.toList()
            orderedIds.forEach { exerciseId ->
                val entries = byExercise[exerciseId].orEmpty()
                if (entries.isEmpty()) return@forEach
                val meta = StrengthCatalog.exerciseForId(exerciseId)
                val setting = entries.lastOrNull { it.setting.isNotBlank() }?.setting.orEmpty()
                val setsText = entries.joinToString(", ") { if (it.amount == 0) "skip" else it.amount.toString() }
                val unitLabel = meta?.unit?.historyLabel(2) ?: "units"
                val settingPart = if (setting.isNotBlank()) " @ ${setting.cell()}" else ""
                val completed = entries.filter { it.amount > 0 }
                val readyNote = if (meta != null && completed.size >= 3 && completed.all { it.amount >= meta.targetTop && it.setting == setting }) {
                    val next = meta.nextSetting(setting)
                    if (next != null) " *(hit top of range — consider progressing to $next)*" else " *(hit top of range)*"
                } else ""
                appendLine("- ${(meta?.name ?: exerciseId).cell()}$settingPart: $setsText $unitLabel$readyNote")
            }
            if (session.note.isNotBlank()) {
                appendLine("- Notes: ${session.note.cell()}")
            }
            appendLine()
        }
    }
    appendLine("## Current routine")
    appendLine()
    appendLine("| Movement | Target | Available recording amounts |")
    appendLine("| --- | --- | --- |")
    exercises.forEach { exercise ->
        appendLine("| ${exercise.name.cell()} | ${exercise.target.cell()} | ${exercise.amounts.joinToString(", ")} ${exercise.unit.historyLabel(2)}; 0 = skip |")
    }
    appendLine()
    appendLine("### Current movement instructions")
    appendLine()
    exercises.forEach { appendLine("- ${it.name}: ${it.description}") }
    appendLine()
    appendLine("## Recent summary: $start through $today (28 workout days)")
    appendLine()
    appendLine("Completed movements: ${recent.count { it.amount > 0 }}. Skips: ${recent.count { it.amount == 0 }}. Days with a completed movement: ${recent.filter { it.amount > 0 }.map { it.day() }.distinct().size}.")
    appendLine("Averages and ranges exclude skips. Latest entries run oldest to newest.")
    appendLine()
    appendLine("| Movement | Unit | Completed | Skipped | Average | Range | Latest 5 entries |")
    appendLine("| --- | --- | --- | --- | --- | --- | --- |")
    val groups = recent.groupBy { it.exerciseId }
    val ids = exercises.map { it.id } + groups.keys.filter { id -> exercises.none { it.id == id } }
    ids.forEach { id ->
        val entries = groups[id].orEmpty()
        val metadata = RoutineCatalog.exerciseForId(id)
        val amounts = entries.filter { it.amount > 0 }.map { it.amount }
        val average = if (amounts.isEmpty()) "—" else String.format(Locale.ROOT, "%.1f", amounts.average())
        val range = if (amounts.isEmpty()) "—" else "${amounts.min()}–${amounts.max()}"
        val latest = entries.takeLast(5).joinToString("; ") {
            "${it.day()}: ${if (it.amount == 0) "skip" else it.amount.toString()}"
        }.ifEmpty { "—" }
        appendLine("| ${(metadata?.name ?: id).cell()} | ${metadata?.unit?.historyLabel(2) ?: "units"} | ${amounts.size} | ${entries.count { it.amount == 0 }} | $average | $range | $latest |")
    }
    appendLine()
    appendLine("## Complete saved log (oldest first)")
    appendLine()
    appendLine("Timestamps include UTC offsets; workout dates use the export time zone.")
    appendLine()
    appendLine("| Record | Timestamp | Workout day | Movement | Movement ID | Amount | Unit | Result |")
    appendLine("| --- | --- | --- | --- | --- | --- | --- | --- |")
    ordered.forEach { item ->
        val metadata = RoutineCatalog.exerciseForId(item.exerciseId)
        val time = Instant.ofEpochMilli(item.completedAt).atZone(zone).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        appendLine("| ${item.id} | $time | ${item.day()} | ${(metadata?.name ?: item.exerciseId).cell()} | ${item.exerciseId.cell()} | ${item.amount} | ${metadata?.unit?.historyLabel(item.amount) ?: "units"} | ${if (item.amount == 0) "Skipped" else "Completed"} |")
    }
    if (ordered.isEmpty()) appendLine("No saved movements yet.")
}

private fun String.cell() = replace("|", "\\|").replace("\r", " ").replace("\n", " ")
