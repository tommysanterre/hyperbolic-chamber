package ca.tommysanterre.snackloop

import org.junit.Test
import org.junit.Assert.assertEquals

class MovementDiagramTest {
    @Test
    fun `every exercise has its own artwork`() {
        val art = exercises.map { exercise -> artworkFor(exercise.id) }
        assertEquals(art.size, art.distinct().size)
    }

    @Test
    fun `only saiyan artwork changes in super saiyan mode`() {
        val saiyanIds = setOf("pushups", "squats", "deep_squat", "dead_hang")
        exercises.forEach { exercise ->
            val changed = artworkFor(exercise.id) != artworkFor(exercise.id, superSaiyan = true)
            assertEquals(exercise.id in saiyanIds, changed)
        }
    }
}
