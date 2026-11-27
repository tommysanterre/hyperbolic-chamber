package ca.tommysanterre.snackloop

data class StrengthStep(
    val stepIndex: Int,
    val pairLabel: String,
    val exercise: StrengthExercise,
    val setNumber: Int,
    val totalSets: Int,
    val restAfterSeconds: Int
)

data class ExerciseLastPerformance(
    val setting: String,
    val amounts: List<Int>,
    val readyToProgress: Boolean,
    val suggestedNextSetting: String?
)

data class ActiveStrengthSessionState(
    val session: StrengthSession,
    val template: StrengthTemplate,
    val steps: List<StrengthStep>,
    val logs: List<StrengthSetLog>,
    val nextStepIndex: Int,
    val lastCompletedAt: Long?,
    val lastRestSeconds: Int?
) {
    val isReadyToFinish: Boolean get() = nextStepIndex >= steps.size
    val currentStep: StrengthStep? get() = steps.getOrNull(nextStepIndex)
    val completedSetsCount: Int get() = logs.count { it.amount > 0 }
}

/** Expands a strength template's supersets into a linear working-set sequence. */
fun buildStrengthSteps(template: StrengthTemplate): List<StrengthStep> {
    val steps = mutableListOf<StrengthStep>()
    var index = 0
    template.pairs.forEachIndexed { pairIndex, pair ->
        val isLastPair = pairIndex == template.pairs.lastIndex
        for (round in 1..pair.rounds) {
            steps += StrengthStep(
                stepIndex = index++,
                pairLabel = pair.label,
                exercise = pair.first,
                setNumber = round,
                totalSets = pair.rounds,
                restAfterSeconds = pair.restAfterFirstSeconds
            )
            val isFinalSetOfWorkout = isLastPair && round == pair.rounds
            steps += StrengthStep(
                stepIndex = index++,
                pairLabel = pair.label,
                exercise = pair.second,
                setNumber = round,
                totalSets = pair.rounds,
                restAfterSeconds = if (isFinalSetOfWorkout) 0 else pair.restAfterSecondSeconds
            )
        }
    }
    return steps
}

/** Alternates Strength A and Strength B based on the most recently finished session. */
fun recommendedNextTemplate(
    sessions: List<StrengthSession>,
    templates: List<StrengthTemplate> = StrengthCatalog.templates
): StrengthTemplate {
    val lastFinished = sessions
        .filter { it.finishedAt != null }
        .maxWithOrNull(compareBy<StrengthSession> { it.finishedAt }.thenBy { it.id })
        ?: return templates.first()
    val currentIdx = templates.indexOfFirst { it.id == lastFinished.templateId }
    if (currentIdx < 0) return templates.first()
    return templates[(currentIdx + 1) % templates.size]
}

/** Reconstructs an in-progress strength session purely from persisted session and set rows. */
fun activeStrengthSessionState(
    sessions: List<StrengthSession>,
    setLogs: List<StrengthSetLog>
): ActiveStrengthSessionState? {
    val active = sessions
        .filter { it.finishedAt == null }
        .maxWithOrNull(compareBy<StrengthSession> { it.startedAt }.thenBy { it.id })
        ?: return null
    val template = StrengthCatalog.templateForId(active.templateId) ?: return null
    val steps = buildStrengthSteps(template)
    val sessionLogs = setLogs
        .filter { it.sessionId == active.id }
        .sortedWith(compareBy<StrengthSetLog> { it.stepIndex }.thenBy { it.completedAt }.thenBy { it.id })
    val nextIndex = sessionLogs.size.coerceAtMost(steps.size)
    val lastLog = sessionLogs.lastOrNull()
    val lastRest = lastLog?.let { steps.getOrNull(it.stepIndex)?.restAfterSeconds }?.takeIf { it > 0 }
    return ActiveStrengthSessionState(
        session = active,
        template = template,
        steps = steps,
        logs = sessionLogs,
        nextStepIndex = nextIndex,
        lastCompletedAt = lastLog?.completedAt,
        lastRestSeconds = lastRest
    )
}

/**
 * Computes the last completed performance per exercise for double-progression guidance.
 * An exercise is ready to progress when all 3 sets in the most recent finished session
 * cleanly hit or exceeded [StrengthExercise.targetTop] at the same setting.
 */
fun lastPerformanceByExercise(
    sessions: List<StrengthSession>,
    setLogs: List<StrengthSetLog>,
    templates: List<StrengthTemplate> = StrengthCatalog.templates
): Map<String, ExerciseLastPerformance> {
    val finishedSessionsById = sessions
        .filter { it.finishedAt != null }
        .associateBy { it.id }
    val logsByExercise = setLogs
        .filter { it.sessionId in finishedSessionsById }
        .groupBy { it.exerciseId }

    val result = mutableMapOf<String, ExerciseLastPerformance>()
    templates.flatMap { it.exercises }.forEach { exercise ->
        val logs = logsByExercise[exercise.id].orEmpty()
        if (logs.isEmpty()) return@forEach
        val latestSessionId = logs
            .mapNotNull { finishedSessionsById[it.sessionId] }
            .maxWithOrNull(compareBy<StrengthSession> { it.finishedAt }.thenBy { it.id })
            ?.id
            ?: return@forEach
        val latestLogs = logs
            .filter { it.sessionId == latestSessionId }
            .sortedWith(compareBy<StrengthSetLog> { it.setNumber }.thenBy { it.id })
        val completed = latestLogs.filter { it.amount > 0 }
        if (completed.isEmpty()) return@forEach
        val setting = completed.last().setting.ifBlank { exercise.defaultSetting }
        val sameSettingAllSets = completed.size >= 3 && completed.all { it.setting == setting }
        val readyToProgress = sameSettingAllSets && completed.all { it.amount >= exercise.targetTop }
        result[exercise.id] = ExerciseLastPerformance(
            setting = setting,
            amounts = latestLogs.map { it.amount },
            readyToProgress = readyToProgress,
            suggestedNextSetting = if (readyToProgress) exercise.nextSetting(setting) else null
        )
    }
    return result
}

/** Chooses the default progression setting for the current set: earlier set in this session, else last finished session, else catalog default. */
fun defaultSettingForExercise(
    exercise: StrengthExercise,
    currentSessionLogs: List<StrengthSetLog>,
    lastPerformance: Map<String, ExerciseLastPerformance>
): String {
    currentSessionLogs
        .lastOrNull { it.exerciseId == exercise.id && it.setting.isNotBlank() }
        ?.let { return it.setting }
    lastPerformance[exercise.id]?.setting?.takeIf { it in exercise.settings }?.let { return it }
    return exercise.defaultSetting
}
