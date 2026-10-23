package ca.tommysanterre.snackloop

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals

class MovementDiagramTest {
    @Test
    fun `every exercise has its own artwork in both forms`() {
        listOf(false, true).forEach { superSaiyan ->
            val art = exercises.map { exercise -> artworkFor(exercise.id, superSaiyan) }
            assertEquals(art.size, art.distinct().size)
        }
    }

    @Test
    fun `every active movement transforms in super saiyan mode`() {
        exercises.forEach { exercise ->
            assertNotEquals(exercise.id, artworkFor(exercise.id), artworkFor(exercise.id, superSaiyan = true))
        }
    }
}
