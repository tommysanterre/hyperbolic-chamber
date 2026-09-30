package ca.tommysanterre.snackloop

import org.junit.Test

class MovementDiagramTest {
    @Test
    fun `every exercise has poses that can be constructed`() {
        exercises.forEach { exercise -> posesFor(exercise.id) }
    }
}
