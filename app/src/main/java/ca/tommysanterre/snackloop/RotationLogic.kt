package ca.tommysanterre.snackloop

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class RotationState(
    val currentIndex: Int,
    val rotationsOnDate: Int,
    val completedRotations: Int
)

/** Reconstructs the queue and completed rotations exclusively from persisted history. */
fun rotationState(
    completions: List<Completion>,
    date: LocalDate,
    zoneId: ZoneId
): RotationState {
    var expectedIndex = 0
    var rotationsOnDate = 0
    var completedRotations = 0
    var ringRowsStarted = false

    completions
        .sortedWith(compareBy<Completion> { it.completedAt }.thenBy { it.id })
        .forEach { completion ->
            val completedIndex = exercises.indexOfFirst { it.id == completion.exerciseId }
            if (completedIndex < 0) return@forEach
            if (completion.exerciseId == "ring_rows") ringRowsStarted = true

            // Preserve completed seven-exercise loops recorded before ring rows was added.
            val finishesLegacyLoop = !ringRowsStarted &&
                exercises[expectedIndex].id == "ring_rows" &&
                completion.exerciseId == "hip_flexor"

            // A mismatch should never be written by the app. Resynchronizing from the stored
            // exercise keeps an old or manually altered database usable without inventing a
            // completed rotation.
            if (completedIndex != expectedIndex && !finishesLegacyLoop) {
                expectedIndex = (completedIndex + 1) % exercises.size
                return@forEach
            }

            if (completedIndex == exercises.lastIndex) {
                completedRotations++
                val completionDate = Instant.ofEpochMilli(completion.completedAt)
                    .atZone(zoneId)
                    .toLocalDate()
                if (completionDate == date) rotationsOnDate++
            }
            expectedIndex = (expectedIndex + 1) % exercises.size
        }

    return RotationState(expectedIndex, rotationsOnDate, completedRotations)
}
