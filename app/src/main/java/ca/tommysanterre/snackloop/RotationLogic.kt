package ca.tommysanterre.snackloop

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/** The local workout day runs from 4 AM until 4 AM the following calendar day. */
fun workoutDate(time: ZonedDateTime): LocalDate =
    time.toLocalDate().minusDays(if (time.hour < 4) 1 else 0)

fun nextWorkoutDayStart(time: ZonedDateTime): ZonedDateTime =
    workoutDate(time).plusDays(1).atTime(4, 0).atZone(time.zone)

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
    var currentIndex = 0
    var rotationsOnDate = 0
    var completedRotations = 0
    var activeDate: LocalDate? = null
    var activeRoutineVersion: Int? = null
    var exercisesInLoop = 0

    completions
        .sortedWith(compareBy<Completion> { it.completedAt }.thenBy { it.id })
        .forEach { completion ->
            val routine = exercisesForRoutine(completion.routineVersion)
            val completedIndex = routine.indexOfFirst { it.id == completion.exerciseId }
            if (completedIndex < 0) return@forEach
            val completionDate = workoutDate(
                Instant.ofEpochMilli(completion.completedAt).atZone(zoneId)
            )
            if (completionDate != activeDate || completedIndex != expectedIndex ||
                completion.routineVersion != activeRoutineVersion
            ) {
                // Each day starts a new loop at its first recorded exercise. A broken
                // sequence also starts over, while the queue still follows history.
                exercisesInLoop = 0
                activeDate = completionDate
                activeRoutineVersion = completion.routineVersion
            }

            // Skipping advances the queue, but cannot contribute to a completed loop.
            exercisesInLoop = if (completion.amount > 0) exercisesInLoop + 1 else 0
            expectedIndex = (completedIndex + 1) % routine.size
            currentIndex = nextExerciseIndex(completion.exerciseId)
            if (exercisesInLoop == routine.size) {
                completedRotations++
                if (completionDate == date) rotationsOnDate++
                exercisesInLoop = 0
            }
        }

    return RotationState(currentIndex, rotationsOnDate, completedRotations)
}
