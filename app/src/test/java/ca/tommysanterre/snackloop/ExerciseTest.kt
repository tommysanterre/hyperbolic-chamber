package ca.tommysanterre.snackloop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExerciseTest {
    @Test
    fun `recording buttons match each new target and include skip`() {
        val expected = mapOf(
            "knee_pushups" to (0..8).toList(),
            "squats" to listOf(0, 10, 11, 12, 13, 14, 15),
            "three_point_bridge" to listOf(0, 3, 4, 5),
            "scapular_pullups" to (0..5).toList(),
            "single_leg_rdl" to listOf(0, 5, 6, 7, 8),
            "tuck_l_sit" to listOf(0, 10, 11, 12, 13, 14, 15),
            "reverse_lunges" to listOf(0, 5),
            "ring_rows" to listOf(0, 5, 6, 7, 8, 9, 10),
            "heel_dig_bridges" to (0..8).toList(),
            "bear_plank" to (0..15).toList()
        )
        assertEquals(expected, exercises.associate { it.id to it.recordingAmounts() })
    }

    @Test
    fun `retired history retains movement names and original units`() {
        val expected = mapOf(
            "deep_squat" to ("Deep squat hold" to "seconds"),
            "dead_hang" to ("Dead hang" to "seconds"),
            "hip_flexor" to ("Hip-flexor stretch" to "seconds per side"),
            "pushups" to ("Push-ups" to "reps"),
            "ring_hamstring_curls" to ("Ring hamstring curls" to "reps"),
            "pike_pushups" to ("Pike push-ups" to "reps"),
            "pullups" to ("Chin-ups" to "reps")
        )
        expected.forEach { (id, details) ->
            val exercise = exerciseForId(id)!!
            assertEquals(details.first, exercise.name)
            assertEquals(details.second, exercise.unit.historyLabel(30))
            assertEquals(false, exercises.any { it.id == id })
        }
        assertNull(exerciseForId("unknown"))
    }

    @Test
    fun `new completions use the current routine version`() {
        assertEquals(4, Completion(exerciseId = "scapular_pullups", amount = 3).routineVersion)
    }
}
