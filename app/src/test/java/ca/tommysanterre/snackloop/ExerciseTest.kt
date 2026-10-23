package ca.tommysanterre.snackloop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExerciseTest {
    @Test
    fun `recording buttons match each new target and include skip`() {
        val expected = mapOf(
            "pushups" to listOf(0, 5, 6, 7, 8, 9, 10),
            "squats" to listOf(0, 10, 11, 12, 13, 14, 15),
            "three_point_bridge" to listOf(0, 3, 4, 5),
            "pullups" to listOf(0, 1, 2),
            "single_leg_rdl" to listOf(0, 5, 6, 7, 8),
            "tuck_l_sit" to listOf(0, 5, 10, 15),
            "reverse_lunges" to listOf(0, 5),
            "ring_rows" to listOf(0, 5, 6, 7, 8, 9, 10),
            "ring_hamstring_curls" to listOf(0, 5, 6, 7, 8),
            "pike_pushups" to listOf(0, 3, 4, 5, 6)
        )
        assertEquals(expected, exercises.associate { it.id to it.recordingAmounts() })
    }

    @Test
    fun `retired history retains movement names and original units`() {
        val expected = mapOf(
            "deep_squat" to ("Deep squat hold" to "seconds"),
            "dead_hang" to ("Dead hang" to "seconds"),
            "hip_flexor" to ("Hip-flexor stretch" to "seconds per side")
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
        assertEquals(2, Completion(exerciseId = "pushups", amount = 5).routineVersion)
    }
}
