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
    fun `rotation spanning midnight stays in the previous workout day`() {
        val beforeMidnight = exercises.dropLast(1).mapIndexed { index, exercise ->
            completion(exercise, index, LocalDate.of(2026, 9, 29), 23)
        }
        val finish = completion(exercises.last(), exercises.lastIndex, today, 0)

        assertEquals(0, state(beforeMidnight + finish).rotationsOnDate)
        assertEquals(1, rotationState(beforeMidnight + finish, today.minusDays(1), zone).rotationsOnDate)
        assertEquals(1, state(beforeMidnight + finish).completedRotations)
    }

    @Test
    fun `rotation crossing 4 AM does not count and queue position persists`() {
        val beforeBoundary = exercises.dropLast(1).mapIndexed { index, exercise ->
            completion(exercise, index, today, 3)
        }
        val afterBoundary = exercises.indices.map { offset ->
            completion(exercises[(exercises.lastIndex + offset) % exercises.size], offset, today, 4)
        }

        val partial = state(beforeBoundary + afterBoundary.take(1))
        assertEquals(0, partial.currentIndex)
        assertEquals(0, partial.rotationsOnDate)
        assertEquals(0, partial.completedRotations)
        val full = state(beforeBoundary + afterBoundary)
        assertEquals(exercises.lastIndex, full.currentIndex)
        assertEquals(1, full.rotationsOnDate)
        assertEquals(1, full.completedRotations)
    }

    @Test
    fun `day starting at ring rows counts only on returning to ring rows`() {
        val startIndex = exercises.indexOfFirst { it.id == "ring_rows" }
        val previousDay = exercises.take(startIndex).mapIndexed { index, exercise ->
            completion(exercise, index, today.minusDays(1))
        }
        val currentDay = (0 until exercises.size * 2).map { offset ->
            completion(exercises[(startIndex + offset) % exercises.size], offset)
        }

        assertEquals(startIndex, state(previousDay).currentIndex)
        assertEquals(0, state(previousDay + currentDay.take(2)).completedRotations)
        assertEquals(0, state(previousDay + currentDay.take(exercises.size - 1)).rotationsOnDate)
        val firstLoop = state(previousDay + currentDay.take(exercises.size))
        assertEquals(startIndex, firstLoop.currentIndex)
        assertEquals(1, firstLoop.rotationsOnDate)
        assertEquals(2, state(previousDay + currentDay).completedRotations)
        assertEquals(2, state(previousDay + currentDay).rotationsOnDate)
    }

    @Test
    fun `workout day changes exactly at 4 AM local time`() {
        assertEquals(today.minusDays(1), workoutDate(today.atTime(3, 59, 59).atZone(zone)))
        assertEquals(today, workoutDate(today.atTime(4, 0).atZone(zone)))
        assertEquals(today, workoutDate(today.plusDays(1).atTime(3, 59, 59).atZone(zone)))
        assertEquals(today.atTime(4, 0).atZone(zone), nextWorkoutDayStart(today.atTime(3, 59).atZone(zone)))
        assertEquals(today.plusDays(1).atTime(4, 0).atZone(zone), nextWorkoutDayStart(today.atTime(4, 0).atZone(zone)))
    }

    @Test
    fun `4 AM boundary follows local time through daylight saving changes`() {
        listOf(LocalDate.of(2026, 3, 8), LocalDate.of(2026, 11, 1)).forEach { date ->
            val beforeBoundary = date.atTime(3, 30).atZone(zone)
            assertEquals(date.minusDays(1), workoutDate(beforeBoundary))
            assertEquals(date.atTime(4, 0).atZone(zone), nextWorkoutDayStart(beforeBoundary))
            assertEquals(date, workoutDate(date.atTime(4, 0).atZone(zone)))
        }
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
    fun `seven exercise history cannot count a loop missing ring rows`() {
        val legacy = exercises.filter { it.id != "ring_rows" }
            .mapIndexed { index, exercise -> completion(exercise, index, today.minusDays(1)) }
        val current = exercises.mapIndexed { index, exercise -> completion(exercise, index) }

        assertEquals(0, state(legacy).completedRotations)
        assertEquals(1, state(legacy + current).completedRotations)
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

        assertEquals(0, state(history.reversed()).completedRotations)
        assertEquals(0, state(history.reversed()).rotationsOnDate)
        assertEquals(0, state(history.dropLast(1)).completedRotations)
    }

    @Test
    fun `skips and completed movements reconstruct the same queue after restart`() {
        val history = exercises.mapIndexed { index, exercise ->
            completion(exercise, index).copy(amount = if (index % 2 == 0) 0 else 5)
        }

        assertEquals(0, state(history.reversed()).currentIndex)
        assertEquals(0, state(history.reversed()).completedRotations)
        assertEquals(exercises.lastIndex, state(history.dropLast(1)).currentIndex)
    }

    @Test
    fun `a full sequence after a skip can complete a loop`() {
        val skipped = completion(exercises.first(), 0).copy(amount = 0)
        val following = (1..exercises.size).map { offset ->
            completion(exercises[offset % exercises.size], offset)
        }

        assertEquals(0, state(listOf(skipped) + following.dropLast(1)).completedRotations)
        assertEquals(1, state(listOf(skipped) + following).completedRotations)
        assertEquals(1, state(listOf(skipped) + following).currentIndex)
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
