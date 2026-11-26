package ca.tommysanterre.snackloop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseTest {
    @Test
    fun `recording buttons match each target and include skip`() {
        val expected = mapOf(
            "dead_hang" to listOf(0, 5, 10, 15, 20, 25, 30),
            "deep_squat" to listOf(0, 5, 10, 15, 20, 25, 30),
            "band_pull_aparts" to listOf(0) + (5..15).toList(),
            "three_point_bridge" to listOf(0, 3, 4, 5),
            "scapular_pullups" to (0..8).toList(),
            "hip_flexor" to listOf(0, 5, 10, 15, 20, 25, 30),
            "squats" to listOf(0) + (5..15).toList(),
            "bear_plank" to (0..15).toList()
        )
        assertEquals(expected, RoutineCatalog.exercises.associate { it.id to it.recordingAmounts() })
    }

    @Test
    fun `exercise lookup handles known and unknown IDs and out-of-range indices`() {
        RoutineCatalog.exercises.forEach { exercise ->
            assertEquals(exercise, RoutineCatalog.exerciseForId(exercise.id))
            assertTrue(exercise.amounts.isNotEmpty() && exercise.amounts.all { it > 0 })
        }
        assertNull(RoutineCatalog.exerciseForId("unknown"))
        assertEquals(RoutineCatalog.exercises.first(), RoutineCatalog.exerciseAt(-1))
        assertEquals(RoutineCatalog.exercises.first(), RoutineCatalog.exerciseAt(999))
    }

    @Test
    fun `unit labels and accessibility descriptions handle skips, singular, and plural`() {
        val hipFlexor = RoutineCatalog.exerciseForId("hip_flexor")!!

        assertEquals("1 rep", "1 ${UnitType.REPS.historyLabel(1)}")
        assertEquals("8 reps per side", "8 ${UnitType.REPS_PER_SIDE.historyLabel(8)}")
        assertEquals("30 seconds per side", "30 ${UnitType.SECONDS_PER_SIDE.historyLabel(30)}")
        assertEquals(
            "Skip Hip-flexor stretch and go to next exercise",
            hipFlexor.recordingActionDescription(0)
        )
        assertEquals(
            "Record 20 seconds per side and go to next exercise",
            hipFlexor.recordingActionDescription(20)
        )
    }
}
