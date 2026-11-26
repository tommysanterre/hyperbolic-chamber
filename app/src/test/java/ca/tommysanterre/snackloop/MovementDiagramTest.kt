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
            val art = RoutineCatalog.exercises.map { exercise ->
                exercise.artwork.drawable(superSaiyan)
            }
            assertEquals(art.size, art.distinct().size)
        }
    }

    @Test
    fun `every active movement transforms in super saiyan mode`() {
        RoutineCatalog.exercises.forEach { exercise ->
            assertNotEquals(
                exercise.id,
                exercise.artwork.drawable(superSaiyan = false),
                exercise.artwork.drawable(superSaiyan = true)
            )
        }
    }
}
