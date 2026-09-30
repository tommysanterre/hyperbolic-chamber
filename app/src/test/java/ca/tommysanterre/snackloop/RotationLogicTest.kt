package ca.tommysanterre.snackloop

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class RotationLogicTest {
    private val zone = ZoneId.of("America/Toronto")
    private val today = LocalDate.of(2026, 9, 30)

    @Test
    fun `exercise advances in order and wraps after hip flexor stretch`() {
        assertEquals(0, state(emptyList()).currentIndex)

        val history = mutableListOf<Completion>()
        exercises.forEachIndexed { index, exercise ->
            history += completion(exercise, index)
            assertEquals((index + 1) % exercises.size, state(history).currentIndex)
        }

        assertEquals(1, state(history).rotationsOnDate)
    }

    @Test
    fun `queue position reconstructs from persisted ordered history`() {
        val history = (0..9).map { offset ->
            completion(exercises[offset % exercises.size], offset)
        }

        assertEquals(3, state(history).currentIndex)
    }

    @Test
    fun `rotation spanning midnight is credited when it finishes`() {
        val beforeMidnight = exercises.dropLast(1).mapIndexed { index, exercise ->
            completion(exercise, index, LocalDate.of(2026, 9, 29), 23)
        }
        val finish = completion(exercises.last(), 6, today, 0)

        assertEquals(1, state(beforeMidnight + finish).rotationsOnDate)
        assertEquals(0, rotationState(beforeMidnight + finish, today.minusDays(1), zone).rotationsOnDate)
    }

    @Test
    fun `multiple rotations completed today are all counted`() {
        val history = (0 until exercises.size * 2).map { offset ->
            completion(exercises[offset % exercises.size], offset)
        }

        assertEquals(2, state(history).rotationsOnDate)
    }

    @Test
    fun `out of order records do not invent a completed rotation`() {
        val incompleteSequence = listOf(
            completion(exercises.first(), 0),
            completion(exercises.last(), 1)
        )

        assertEquals(0, state(incompleteSequence).rotationsOnDate)
        assertEquals(0, state(incompleteSequence).currentIndex)
    }

    @Test
    fun `undo moves back and removes the rotation closed by latest completion`() {
        val fullRotation = exercises.mapIndexed { index, exercise -> completion(exercise, index) }

        val afterUndo = state(fullRotation.dropLast(1))

        assertEquals(exercises.lastIndex, afterUndo.currentIndex)
        assertEquals(0, afterUndo.rotationsOnDate)
    }

    @Test
    fun `exercise metadata preserves reps seconds and per-side wording`() {
        assertEquals(
            listOf(
                "pushups",
                "squats",
                "deep_squat",
                "pullups",
                "dead_hang",
                "reverse_lunges",
                "hip_flexor"
            ),
            exercises.map { it.id }
        )
        assertEquals(
            listOf(
                UnitType.REPS,
                UnitType.REPS,
                UnitType.SECONDS,
                UnitType.REPS,
                UnitType.SECONDS,
                UnitType.REPS_PER_SIDE,
                UnitType.SECONDS_PER_SIDE
            ),
            exercises.map { it.unit }
        )
        assertEquals(
            listOf(
                "5–10 reps",
                "10–15 reps",
                "30 seconds",
                "2–5 full pull-ups",
                "30 seconds",
                "5 reps per side",
                "30 seconds per side"
            ),
            exercises.map { it.target }
        )
        assertEquals("1 rep", "1 ${UnitType.REPS.historyLabel(1)}")
        assertEquals("8 reps per side", "8 ${UnitType.REPS_PER_SIDE.historyLabel(8)}")
        assertEquals("30 seconds per side", "30 ${UnitType.SECONDS_PER_SIDE.historyLabel(30)}")
    }

    private fun state(history: List<Completion>) = rotationState(history, today, zone)

    private fun completion(
        exercise: Exercise,
        offsetMinutes: Int,
        date: LocalDate = today,
        hour: Int = 12
    ) = Completion(
        id = offsetMinutes.toLong() + 1,
        exerciseId = exercise.id,
        amount = 5,
        completedAt = ZonedDateTime.of(date.atTime(hour, 0).plusMinutes(offsetMinutes.toLong()), zone)
            .toInstant()
            .toEpochMilli()
    )
}
