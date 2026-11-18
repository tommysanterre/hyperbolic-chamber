package ca.tommysanterre.snackloop

import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** A portable text report: recent amounts for review, followed by the complete saved log. */
internal fun historyExport(
    completions: List<Completion>,
    generatedAt: ZonedDateTime = ZonedDateTime.now()
): String = buildString {
    val zone = generatedAt.zone
    val today = workoutDate(generatedAt)
    val start = today.minusDays(27)
    val ordered = completions.sortedWith(compareBy<Completion> { it.completedAt }.thenBy { it.id })
    fun Completion.day() = workoutDate(Instant.ofEpochMilli(completedAt).atZone(zone))
    val recent = ordered.filter { it.day() in start..today }
    val rotation = rotationState(ordered, today, zone)

    appendLine("# Hyperbolic Time Chamber — workout history")
    appendLine()
    appendLine("Exported: ${generatedAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}")
    appendLine("Time zone: $zone. Workout days run from 4 AM to 4 AM in this zone.")
    appendLine("Current routine: $CURRENT_ROUTINE_VERSION. Saved records: ${ordered.size}.")
    appendLine("Completed loops (all time): ${rotation.completedRotations}.")
    appendLine()
    appendLine("## Context to add when asking about difficulty")
    appendLine()
    appendLine("Share how hard each movement felt, how many more clean reps or seconds you could manage, and any assistance, setup changes or discomfort. These details are not recorded by the app.")
    appendLine("Amounts are self-reported. A skip does not explain why a movement was skipped. Reaching the highest recording button does not by itself mean a movement is ready to progress.")
    appendLine("Per-side amounts are recorded per side, not combined. Undone entries are no longer in the saved log.")
    appendLine()
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
    appendLine("Averages and ranges exclude skips. Latest entries run oldest to newest. Earlier movement variations are listed separately.")
    appendLine()
    appendLine("| Movement | Unit | Completed | Skipped | Average | Range | Latest 5 entries |")
    appendLine("| --- | --- | --- | --- | --- | --- | --- |")
    val groups = recent.groupBy { it.exerciseId }
    val ids = exercises.map { it.id } + groups.keys.filter { id -> exercises.none { it.id == id } }
    ids.forEach { id ->
        val entries = groups[id].orEmpty()
        val metadata = entries.lastOrNull()?.metadata() ?: exerciseForId(id)
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
    appendLine("Timestamps include UTC offsets; workout dates use the export time zone. Historical movement names and units follow their recorded routine version. Targets above describe the current routine only.")
    appendLine()
    appendLine("| Record | Timestamp | Workout day | Movement | Movement ID | Amount | Unit | Result | Routine |")
    appendLine("| --- | --- | --- | --- | --- | --- | --- | --- | --- |")
    ordered.forEach { item ->
        val metadata = item.metadata()
        val time = Instant.ofEpochMilli(item.completedAt).atZone(zone).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        appendLine("| ${item.id} | $time | ${item.day()} | ${(metadata?.name ?: item.exerciseId).cell()} | ${item.exerciseId.cell()} | ${item.amount} | ${metadata?.unit?.historyLabel(item.amount) ?: "units"} | ${if (item.amount == 0) "Skipped" else "Completed"} | ${item.routineVersion} |")
    }
    if (ordered.isEmpty()) appendLine("No saved movements yet.")
}

private fun Completion.metadata(): Exercise? =
    exercisesForRoutine(routineVersion).firstOrNull { it.id == exerciseId } ?: exerciseForId(exerciseId)

private fun String.cell() = replace("|", "\\|").replace("\r", " ").replace("\n", " ")
