package ca.tommysanterre.snackloop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseTest {
    @Test
    fun `recording buttons match each new target and include skip`() {
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
        assertEquals(expected, RoutineCatalog.currentExercises.associate { it.id to it.recordingAmounts() })
    }

    @Test
    fun `retired history retains movement names and original units`() {
        val expected = mapOf(
            "pushups" to ("Push-ups" to "reps"),
            "knee_pushups" to ("Knee push-ups" to "reps"),
            "single_leg_rdl" to ("Supported single-leg Romanian deadlift" to "reps per side"),
            "tuck_l_sit" to ("Tuck L-sit" to "seconds"),
            "reverse_lunges" to ("Reverse lunges" to "reps per side"),
            "ring_rows" to ("Ring rows" to "reps"),
            "ring_hamstring_curls" to ("Ring hamstring curls" to "reps"),
            "heel_dig_bridges" to ("Heel-dig bridges" to "reps"),
            "pike_pushups" to ("Pike push-ups" to "reps"),
            "pullups" to ("Chin-ups" to "reps")
        )
        expected.forEach { (id, details) ->
            val exercise = RoutineCatalog.exerciseForId(id)!!
            assertEquals(details.first, exercise.name)
            assertEquals(details.second, exercise.unit.historyLabel(30))
            assertEquals(false, RoutineCatalog.currentExercises.any { it.id == id })
        }
        assertNull(RoutineCatalog.exerciseForId("unknown"))
    }

    @Test
    fun `new completions use the current routine version`() {
        assertEquals(5, Completion(exerciseId = "band_pull_aparts", amount = 10).routineVersion)
    }

    @Test
    fun `resolving a completion honors its recorded routine version before falling back`() {
        val v1Hang = Completion(exerciseId = "dead_hang", amount = 30, routineVersion = 1)
        val v5Hang = Completion(exerciseId = "dead_hang", amount = 20, routineVersion = 5)
        val v2LSit = Completion(exerciseId = "tuck_l_sit", amount = 10, routineVersion = 2)
        val v4LSit = Completion(exerciseId = "tuck_l_sit", amount = 10, routineVersion = 4)
        val unknownVersionChinUp = Completion(exerciseId = "pullups", amount = 1, routineVersion = 99)

        assertEquals("30 seconds", RoutineCatalog.resolve(v1Hang)?.target)
        assertEquals("20–30 seconds", RoutineCatalog.resolve(v5Hang)?.target)
        assertEquals(listOf(5, 10, 15), RoutineCatalog.resolve(v2LSit)?.amounts)
        assertEquals((1..15).toList(), RoutineCatalog.resolve(v4LSit)?.amounts)
        assertEquals("Chin-ups", RoutineCatalog.resolve(unknownVersionChinUp)?.name)
        assertNull(RoutineCatalog.resolve(Completion(exerciseId = "unknown", amount = 1, routineVersion = 99)))
    }

    @Test
    fun `every exercise across all versions advances to a valid current slot and unknown IDs default safely`() {
        (1..RoutineCatalog.CURRENT_VERSION).forEach { version ->
            val routine = RoutineCatalog.exercisesForVersion(version)
            assertTrue("Version $version should not be empty", routine.isNotEmpty())
            routine.forEach { exercise ->
                val nextIndex = RoutineCatalog.nextExerciseIndex(exercise.id)
                assertTrue(
                    "Invalid next index $nextIndex for ${exercise.id} in version $version",
                    nextIndex in RoutineCatalog.currentExercises.indices
                )
            }
        }
        assertEquals(0, RoutineCatalog.nextExerciseIndex("unknown"))
        assertEquals(RoutineCatalog.currentExercises.first(), RoutineCatalog.exerciseAt(-1))
        assertEquals(RoutineCatalog.currentExercises.first(), RoutineCatalog.exerciseAt(999))
    }

    @Test
    fun `recorded amount formatting and accessibility labels handle skips, singular, plural and unknown`() {
        val chinUps = RoutineCatalog.exerciseForId("pullups")!!
        val hipFlexor = RoutineCatalog.exerciseForId("hip_flexor")!!

        assertEquals("Skipped", RoutineCatalog.formatRecordedAmount(0, chinUps))
        assertEquals("1 rep", RoutineCatalog.formatRecordedAmount(1, chinUps))
        assertEquals("20 seconds per side", RoutineCatalog.formatRecordedAmount(20, hipFlexor))
        assertEquals("7 units", RoutineCatalog.formatRecordedAmount(7, null))

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
