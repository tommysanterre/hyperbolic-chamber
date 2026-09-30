package ca.tommysanterre.snackloop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

data class UiState(
    val completions: List<Completion> = emptyList(),
    val currentIndex: Int = 0,
    val rotationsToday: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = SnackLoopDatabase.get(application).completionDao()
    private val localDate = flow {
        while (true) {
            val zone = ZoneId.systemDefault()
            val now = ZonedDateTime.now(zone)
            emit(now.toLocalDate())
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)
            delay(Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1_000))
        }
    }

    val state = combine(dao.observeAll(), localDate) { history, today ->
        val zone = ZoneId.systemDefault()
        val rotation = rotationState(history, today, zone)
        UiState(history, rotation.currentIndex, rotation.rotationsOnDate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    fun complete(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch {
            dao.insert(Completion(exerciseId = exercises[state.value.currentIndex].id, amount = amount))
        }
    }

    fun undo() = viewModelScope.launch { dao.last()?.let { dao.delete(it) } }
}
