package ca.tommysanterre.snackloop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MovementDiagramTest {
    @Test
    fun `one completed loop unlocks super saiyan even on a later day`() {
        assertFalse(UiState().superSaiyanUnlocked)
        assertTrue(UiState(rotationsToday = 1, completedRotations = 1).superSaiyanUnlocked)
        assertTrue(UiState(rotationsToday = 0, completedRotations = 1).superSaiyanUnlocked)
    }

    @Test
    fun `every exercise has its own artwork in both forms`() {
        listOf(false, true).forEach { superSaiyan ->
            val art = RoutineCatalog.currentExercises.map { exercise ->
                exercise.artwork.drawable(superSaiyan)
            }
            assertEquals(art.size, art.distinct().size)
        }
    }

    @Test
    fun `every active movement transforms in super saiyan mode`() {
        RoutineCatalog.currentExercises.forEach { exercise ->
            assertNotEquals(
                exercise.id,
                exercise.artwork.drawable(superSaiyan = false),
                exercise.artwork.drawable(superSaiyan = true)
            )
        }
    }

    @Test
    fun `active movements split evenly while historical single-leg deadlift retains its narrower start panel`() {
        val sampleWidth = 2400
        RoutineCatalog.currentExercises.forEach { exercise ->
            assertEquals(exercise.id, 1200, exercise.artwork.splitX(sampleWidth))
        }
        val historicalRdl = RoutineCatalog.exerciseForId("single_leg_rdl")!!
        assertEquals(1100, historicalRdl.artwork.splitX(sampleWidth))
    }

    @Test
    fun `all historical routine exercises define valid panel split coordinates`() {
        val sampleWidth = 1200
        (1..RoutineCatalog.CURRENT_VERSION).forEach { version ->
            RoutineCatalog.exercisesForVersion(version).forEach { exercise ->
                val split = exercise.artwork.splitX(sampleWidth)
                assertTrue("${exercise.id} split $split out of bounds", split in 1 until sampleWidth)
            }
        }
    }
}
