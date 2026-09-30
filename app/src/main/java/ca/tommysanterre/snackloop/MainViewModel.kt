package ca.tommysanterre.snackloop

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

data class UiState(
    val completions: List<Completion> = emptyList(),
    val currentIndex: Int = 0,
    val rotationsToday: Int = 0,
    val completedRotations: Int = 0,
    val storageUnavailable: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val storageUnavailable = MutableStateFlow(false)
    private val dao = runCatching { SnackLoopDatabase.get(application).completionDao() }
        .onFailure(::reportStorageFailure)
        .getOrNull()
    private val localDate = flow {
        while (true) {
            val zone = ZoneId.systemDefault()
            val now = ZonedDateTime.now(zone)
            emit(now.toLocalDate())
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)
            delay(Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1_000))
        }
    }

    private val history = dao?.observeAll()
        ?.catch {
            reportStorageFailure(it)
            emit(emptyList())
        }
        ?: flowOf(emptyList())

    val state = combine(history, localDate, storageUnavailable) { history, today, unavailable ->
        val zone = ZoneId.systemDefault()
        val rotation = rotationState(history, today, zone)
        UiState(history, rotation.currentIndex, rotation.rotationsOnDate, rotation.completedRotations, unavailable)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    fun complete(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch {
            runCatching {
                dao?.insert(Completion(exerciseId = exercises[state.value.currentIndex].id, amount = amount))
                    ?: error("Completion storage is unavailable")
            }.onFailure(::reportStorageFailure)
        }
    }

    fun undo() = viewModelScope.launch {
        runCatching {
            val availableDao = dao ?: error("Completion storage is unavailable")
            availableDao.last()?.let { availableDao.delete(it) }
        }.onFailure(::reportStorageFailure)
    }

    private fun reportStorageFailure(error: Throwable) {
        Log.e(TAG, "Completion storage is unavailable", error)
        storageUnavailable.value = true
    }

    private companion object {
        const val TAG = "MainViewModel"
    }
}
