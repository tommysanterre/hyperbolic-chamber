package ca.tommysanterre.snackloop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class UiState(
    val completions: List<Completion> = emptyList(),
    val currentIndex: Int = 0,
    val rotationsToday: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = SnackLoopDatabase.get(application).completionDao()

    val state = dao.observeAll().map { history ->
        val chronological = history.sortedWith(compareBy<Completion> { it.completedAt }.thenBy { it.id })
        val currentIndex = chronological.size % exercises.size
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val todayCount = chronological.count {
            Instant.ofEpochMilli(it.completedAt).atZone(zone).toLocalDate() == today
        }
        UiState(history, currentIndex, todayCount / exercises.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    fun complete(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch {
            dao.insert(Completion(exerciseId = exercises[state.value.currentIndex].id, amount = amount))
        }
    }

    fun undo() = viewModelScope.launch { dao.last()?.let { dao.delete(it) } }
}
