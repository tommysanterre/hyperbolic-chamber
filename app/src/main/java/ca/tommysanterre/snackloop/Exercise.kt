package ca.tommysanterre.snackloop

import androidx.annotation.DrawableRes

enum class UnitType { REPS, SECONDS, REPS_PER_SIDE, SECONDS_PER_SIDE }

data class MovementArtwork(
    @param:DrawableRes @get:DrawableRes val baseDrawable: Int,
    @param:DrawableRes @get:DrawableRes val superSaiyanDrawable: Int = baseDrawable
) {
    @DrawableRes
    fun drawable(superSaiyan: Boolean): Int =
        if (superSaiyan) superSaiyanDrawable else baseDrawable
}

data class Exercise(
    val id: String,
    val name: String,
    val target: String,
    val unit: UnitType,
    val description: String,
    val artwork: MovementArtwork,
    val amounts: List<Int> = (5..30 step 5).toList()
) {
    fun recordingAmounts(): List<Int> = listOf(0) + amounts

    fun recordingActionDescription(amount: Int): String =
        if (amount == 0) "Skip $name and go to next exercise"
        else "Record $amount ${unit.historyLabel(amount)} and go to next exercise"
}

fun UnitType.inputLabel() = when (this) {
    UnitType.REPS -> "Reps completed"
    UnitType.SECONDS -> "Seconds held"
    UnitType.REPS_PER_SIDE -> "Reps per side"
    UnitType.SECONDS_PER_SIDE -> "Seconds per side"
}

fun UnitType.historyLabel(amount: Int) = when (this) {
    UnitType.REPS -> if (amount == 1) "rep" else "reps"
    UnitType.SECONDS -> if (amount == 1) "second" else "seconds"
    UnitType.REPS_PER_SIDE -> if (amount == 1) "rep per side" else "reps per side"
    UnitType.SECONDS_PER_SIDE -> if (amount == 1) "second per side" else "seconds per side"
}

/**
 * Current daily movement snack routine.
 * Increment [VERSION] whenever the routine changes to reset the local database cleanly.
 */
object RoutineCatalog {
    const val VERSION = 5

    val exercises: List<Exercise> = listOf(
        Exercise(
            id = "dead_hang",
            name = "Dead hang",
            target = "20–30 seconds",
            unit = UnitType.SECONDS,
            description = "Hang from the bar with straight arms and let your shoulders settle comfortably without shrugging forcefully. Breathe steadily and step down well before your grip fails.",
            artwork = MovementArtwork(R.drawable.movement_dead_hang, R.drawable.movement_dead_hang_ssj),
            amounts = listOf(5, 10, 15, 20, 25, 30)
        ),
        Exercise(
            id = "deep_squat",
            name = "Deep squat hold",
            target = "20–30 seconds",
            unit = UnitType.SECONDS,
            description = "Sink into a comfortable deep squat with both feet planted and hold the bottom position while breathing steadily. Keep your chest upright and hold a light support if your heels lift.",
            artwork = MovementArtwork(R.drawable.movement_deep_squat, R.drawable.movement_deep_squat_ssj),
            amounts = listOf(5, 10, 15, 20, 25, 30)
        ),
        Exercise(
            id = "band_pull_aparts",
            name = "Band pull-aparts",
            target = "10–15 reps",
            unit = UnitType.REPS,
            description = "Stand tall holding a single strand of a light resistance band straight out at chest height with straight arms and shoulders relaxed down. Pull your hands wide until the band touches your chest, squeezing your shoulder blades together, then return slowly with control.",
            artwork = MovementArtwork(R.drawable.movement_band_pull_aparts, R.drawable.movement_band_pull_aparts_ssj),
            amounts = (5..15).toList()
        ),
        Exercise(
            id = "three_point_bridge",
            name = "Three-point bridge",
            target = "3–5 reps per side",
            unit = UnitType.REPS_PER_SIDE,
            description = "Sit with knees bent, feet flat and one hand planted behind you. Press through both feet and the supporting hand to lift your hips, gently turn your chest upward and reach your free arm overhead. Lower with control, then repeat on the other side. Use a comfortable, pain-free range.",
            artwork = MovementArtwork(R.drawable.movement_three_point_bridge, R.drawable.movement_three_point_bridge_ssj),
            amounts = (3..5).toList()
        ),
        Exercise(
            id = "scapular_pullups",
            name = "Scapular pull-ups",
            target = "3–5 reps",
            unit = UnitType.REPS,
            description = "Grip the bar with palms facing away, about shoulder-width apart, and hang with straight elbows. Draw your shoulders smoothly down away from your ears to lift your body a few centimetres, pause briefly, then lower slowly without swinging. Keep these sets easy and well short of failure.",
            artwork = MovementArtwork(R.drawable.movement_scapular_pullups, R.drawable.movement_scapular_pullups_ssj),
            amounts = (1..8).toList()
        ),
        Exercise(
            id = "hip_flexor",
            name = "Hip-flexor stretch",
            target = "20–30 seconds per side",
            unit = UnitType.SECONDS_PER_SIDE,
            description = "From a half-kneeling position with a pad under the trailing knee if desired, gently tuck your pelvis and shift your hips forward until you feel a comfortable stretch at the front of the trailing hip. Stay tall and breathe steadily on each side.",
            artwork = MovementArtwork(R.drawable.movement_hip_flexor, R.drawable.movement_hip_flexor_ssj),
            amounts = listOf(5, 10, 15, 20, 25, 30)
        ),
        Exercise(
            id = "squats",
            name = "Squats",
            target = "10 reps",
            unit = UnitType.REPS,
            description = "Stand comfortably, sit your hips down and back at an easy pace, then stand tall again. Keep the set smooth and low-fatigue to lubricate your knees and hips.",
            artwork = MovementArtwork(R.drawable.movement_squats, R.drawable.movement_squats_ssj),
            amounts = (5..15).toList()
        ),
        Exercise(
            id = "bear_plank",
            name = "Bear plank hold",
            target = "10–15 seconds",
            unit = UnitType.SECONDS,
            description = "Start on hands (or low parallettes for neutral wrists) and knees, wrists under shoulders and knees under hips. Tuck your toes, press the floor away, and hover your knees just a few centimetres with a level back. Breathe normally and stop well before form breaks.",
            artwork = MovementArtwork(R.drawable.movement_bear_plank, R.drawable.movement_bear_plank_ssj),
            amounts = (1..15).toList()
        )
    )

    private val byId: Map<String, Exercise> = exercises.associateBy { it.id }

    init {
        check(exercises.isNotEmpty()) { "Routine must not be empty" }
        check(byId.size == exercises.size) { "Duplicate exercise ID in routine" }
        exercises.forEach { exercise ->
            check(exercise.amounts.isNotEmpty() && exercise.amounts.all { it > 0 }) {
                "Recording amounts for ${exercise.id} must be positive"
            }
        }
    }

    fun exerciseAt(index: Int): Exercise =
        exercises.getOrElse(index) { exercises.first() }

    fun exerciseForId(id: String): Exercise? = byId[id]
}
