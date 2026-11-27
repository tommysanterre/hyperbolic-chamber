package ca.tommysanterre.snackloop

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.max

data class UiState(
    val completions: List<Completion> = emptyList(),
    val currentIndex: Int = 0,
    val rotationsToday: Int = 0,
    val completedRotations: Int = 0,
    val strengthSessions: List<StrengthSession> = emptyList(),
    val strengthSetLogs: List<StrengthSetLog> = emptyList(),
    val activeStrength: ActiveStrengthSessionState? = null,
    val recommendedTemplate: StrengthTemplate = StrengthCatalog.strengthA,
    val lastPerformance: Map<String, ExerciseLastPerformance> = emptyMap(),
    val completedStrengthSessions: Int = 0,
    val pendingTransformation: TransformationCelebration? = null,
    val storageUnavailable: Boolean = false
) {
    val superSaiyanUnlocked: Boolean get() = completedRotations > 0 || completedStrengthSessions > 0
    val hasAnyHistory: Boolean get() = completions.isNotEmpty() || completedStrengthSessions > 0
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val storageUnavailable = MutableStateFlow(false)
    private val pendingTransformation = MutableStateFlow<TransformationCelebration?>(null)
    private val database = runCatching { SnackLoopDatabase.get(application) }
        .onFailure(::reportStorageFailure)
        .getOrNull()
    private val completionDao = database?.completionDao()
    private val strengthDao = database?.strengthDao()

    private val localDate = flow {
        while (true) {
            val zone = ZoneId.systemDefault()
            val now = ZonedDateTime.now(zone)
            emit(workoutDate(now))
            val nextDay = nextWorkoutDayStart(now)
            delay(Duration.between(now, nextDay).toMillis().coerceIn(1_000, 60_000))
        }
    }

    private val history = completionDao?.observeAll()
        ?.catch {
            reportStorageFailure(it)
            emit(emptyList())
        }
        ?: flowOf(emptyList())

    private val strengthSessions = strengthDao?.observeSessions()
        ?.catch {
            reportStorageFailure(it)
            emit(emptyList())
        }
        ?: flowOf(emptyList())

    private val strengthSetLogs = strengthDao?.observeSetLogs()
        ?.catch {
            reportStorageFailure(it)
            emit(emptyList())
        }
        ?: flowOf(emptyList())

    private val snackAndDate = combine(history, localDate) { completions, today ->
        completions to today
    }

    private val strengthData = combine(strengthSessions, strengthSetLogs) { sessions, logs ->
        sessions to logs
    }

    val state = combine(
        snackAndDate,
        strengthData,
        pendingTransformation,
        storageUnavailable
    ) { (completions, today), (sessions, logs), celebration, unavailable ->
        val zone = ZoneId.systemDefault()
        val rotation = rotationState(completions, today, zone)
        val active = activeStrengthSessionState(sessions, logs)
        val recommended = recommendedNextTemplate(sessions)
        val lastPerf = lastPerformanceByExercise(sessions, logs)
        val completedStrength = sessions.count { it.finishedAt != null }
        UiState(
            completions = completions,
            currentIndex = rotation.currentIndex,
            rotationsToday = rotation.rotationsOnDate,
            completedRotations = rotation.completedRotations,
            strengthSessions = sessions,
            strengthSetLogs = logs,
            activeStrength = active,
            recommendedTemplate = recommended,
            lastPerformance = lastPerf,
            completedStrengthSessions = completedStrength,
            pendingTransformation = celebration,
            storageUnavailable = unavailable
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    fun complete(amount: Int) {
        if (amount < 0) return
        viewModelScope.launch {
            runCatching {
                completionDao?.insert(
                    Completion(exerciseId = RoutineCatalog.exerciseAt(state.value.currentIndex).id, amount = amount)
                ) ?: error("Completion storage is unavailable")
            }.onFailure(::reportStorageFailure)
        }
    }

    fun undo() = viewModelScope.launch {
        runCatching {
            val availableDao = completionDao ?: error("Completion storage is unavailable")
            availableDao.last()?.let { availableDao.delete(it) }
        }.onFailure(::reportStorageFailure)
    }

    fun startStrengthSession(templateId: String) = viewModelScope.launch {
        if (StrengthCatalog.templateForId(templateId) == null) return@launch
        runCatching {
            val dao = strengthDao ?: error("Strength storage is unavailable")
            if (state.value.activeStrength != null) return@launch
            dao.insertSession(StrengthSession(templateId = templateId))
        }.onFailure(::reportStorageFailure)
    }

    fun recordStrengthSet(amount: Int, setting: String) {
        if (amount < 0) return
        val active = state.value.activeStrength ?: return
        val step = active.currentStep ?: return
        viewModelScope.launch {
            runCatching {
                val dao = strengthDao ?: error("Strength storage is unavailable")
                dao.insertSetLog(
                    StrengthSetLog(
                        sessionId = active.session.id,
                        stepIndex = step.stepIndex,
                        exerciseId = step.exercise.id,
                        setNumber = step.setNumber,
                        amount = amount,
                        setting = setting.ifBlank { step.exercise.defaultSetting }
                    )
                )
            }.onFailure(::reportStorageFailure)
        }
    }

    fun undoStrengthSet() {
        val active = state.value.activeStrength ?: return
        viewModelScope.launch {
            runCatching {
                val dao = strengthDao ?: error("Strength storage is unavailable")
                dao.lastSetLogForSession(active.session.id)?.let { dao.deleteSetLog(it) }
            }.onFailure(::reportStorageFailure)
        }
    }

    fun abandonStrengthSession() {
        val active = state.value.activeStrength ?: return
        viewModelScope.launch {
            runCatching {
                val dao = strengthDao ?: error("Strength storage is unavailable")
                dao.abandonSession(active.session.id)
            }.onFailure(::reportStorageFailure)
        }
    }

    fun finishStrengthSession(note: String) {
        val currentState = state.value
        val active = currentState.activeStrength ?: return
        val wasUnlocked = currentState.superSaiyanUnlocked
        val nowMs = System.currentTimeMillis()
        val durationMin = max(1L, (nowMs - active.session.startedAt + 30_000L) / 60_000L)
        viewModelScope.launch {
            runCatching {
                val dao = strengthDao ?: error("Strength storage is unavailable")
                dao.finishSession(active.session.id, nowMs, note)
                pendingTransformation.value = TransformationCelebration(
                    templateId = active.template.id,
                    templateName = active.template.name,
                    completedSets = active.completedSetsCount,
                    totalSets = active.steps.size,
                    durationMinutes = durationMin,
                    newlyUnlockedSuperSaiyan = !wasUnlocked
                )
            }.onFailure(::reportStorageFailure)
        }
    }

    fun dismissTransformation() {
        pendingTransformation.value = null
    }

    private fun reportStorageFailure(error: Throwable) {
        Log.e(TAG, "Storage is unavailable", error)
        storageUnavailable.value = true
    }

    private companion object {
        const val TAG = "MainViewModel"
    }
}
