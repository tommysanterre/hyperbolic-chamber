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
        assertEquals(1, state(history).completedRotations)
    }

    @Test
    fun `queue position reconstructs from persisted ordered history`() {
        val history = (0..9).map { offset ->
            completion(exercises[offset % exercises.size], offset)
        }

        assertEquals(2, state(history).currentIndex)
    }

    @Test
    fun `rotation spanning midnight is credited when it finishes`() {
        val beforeMidnight = exercises.dropLast(1).mapIndexed { index, exercise ->
            completion(exercise, index, LocalDate.of(2026, 9, 29), 23)
        }
        val finish = completion(exercises.last(), exercises.lastIndex, today, 0)

        assertEquals(1, state(beforeMidnight + finish).rotationsOnDate)
        assertEquals(0, rotationState(beforeMidnight + finish, today.minusDays(1), zone).rotationsOnDate)
    }

    @Test
    fun `multiple rotations completed today are all counted`() {
        val history = (0 until exercises.size * 2).map { offset ->
            completion(exercises[offset % exercises.size], offset)
        }

        assertEquals(2, state(history).rotationsOnDate)
        assertEquals(2, state(history).completedRotations)
    }

    @Test
    fun `completed rotations persist across days and undo removes the second loop`() {
        val firstDay = exercises.mapIndexed { index, exercise ->
            completion(exercise, index, today.minusDays(1))
        }
        val secondDay = exercises.mapIndexed { index, exercise ->
            completion(exercise, index + exercises.size)
        }

        assertEquals(1, state(firstDay).completedRotations)
        assertEquals(2, state(firstDay + secondDay).completedRotations)
        assertEquals(1, state(firstDay + secondDay.dropLast(1)).completedRotations)
        assertEquals(1, state(firstDay + secondDay).rotationsOnDate)
    }

    @Test
    fun `out of order records do not invent a completed rotation`() {
        val incompleteSequence = listOf(
            completion(exercises.first(), 0),
            completion(exercises.last(), 1)
        )

        assertEquals(0, state(incompleteSequence).rotationsOnDate)
        assertEquals(0, state(incompleteSequence).completedRotations)
        assertEquals(0, state(incompleteSequence).currentIndex)
    }

    @Test
    fun `undo moves back and removes the rotation closed by latest completion`() {
        val fullRotation = exercises.mapIndexed { index, exercise -> completion(exercise, index) }

        val afterUndo = state(fullRotation.dropLast(1))

        assertEquals(exercises.lastIndex, afterUndo.currentIndex)
        assertEquals(0, afterUndo.rotationsOnDate)
        assertEquals(0, afterUndo.completedRotations)
    }

    @Test
    fun `old seven exercise loops remain counted when ring rows is introduced`() {
        val legacy = exercises.filter { it.id != "ring_rows" }
            .mapIndexed { index, exercise -> completion(exercise, index, today.minusDays(1)) }
        val current = exercises.mapIndexed { index, exercise -> completion(exercise, index) }

        assertEquals(1, state(legacy).completedRotations)
        assertEquals(2, state(legacy + current).completedRotations)
        assertEquals(1, state(legacy + current).rotationsOnDate)
        assertEquals(0, state(legacy + current).currentIndex)
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
                "ring_rows",
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
                UnitType.REPS,
                UnitType.SECONDS_PER_SIDE
            ),
            exercises.map { it.unit }
        )
        assertEquals(
            listOf(
                "5–10 reps",
                "10–15 reps",
                "30 seconds",
                "1–2 reps",
                "30 seconds",
                "5 reps per side",
                "5–10 reps",
                "30 seconds per side"
            ),
            exercises.map { it.target }
        )
        assertEquals("1 rep", "1 ${UnitType.REPS.historyLabel(1)}")
        assertEquals("8 reps per side", "8 ${UnitType.REPS_PER_SIDE.historyLabel(8)}")
        assertEquals("30 seconds per side", "30 ${UnitType.SECONDS_PER_SIDE.historyLabel(30)}")
    }

    @Test
    fun `zero amounts advance every exercise and undo restores the skipped movement`() {
        val history = mutableListOf<Completion>()
        exercises.forEachIndexed { index, exercise ->
            history += completion(exercise, index).copy(amount = 0)
            assertEquals((index + 1) % exercises.size, state(history).currentIndex)
            assertEquals(index, state(history.dropLast(1)).currentIndex)
        }

        assertEquals(1, state(history.reversed()).completedRotations)
        assertEquals(1, state(history.reversed()).rotationsOnDate)
        assertEquals(0, state(history.dropLast(1)).completedRotations)
    }

    @Test
    fun `skips and completed movements reconstruct the same queue after restart`() {
        val history = exercises.mapIndexed { index, exercise ->
            completion(exercise, index).copy(amount = if (index % 2 == 0) 0 else 5)
        }

        assertEquals(0, state(history.reversed()).currentIndex)
        assertEquals(1, state(history.reversed()).completedRotations)
        assertEquals(exercises.lastIndex, state(history.dropLast(1)).currentIndex)
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
