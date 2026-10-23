package ca.tommysanterre.snackloop

enum class UnitType { REPS, SECONDS, REPS_PER_SIDE, SECONDS_PER_SIDE }

data class Exercise(
    val id: String,
    val name: String,
    val target: String,
    val unit: UnitType,
    val description: String,
    val amounts: List<Int> = (5..30 step 5).toList()
)

fun Exercise.recordingAmounts(): List<Int> = listOf(0) + amounts

const val CURRENT_ROUTINE_VERSION = 2

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

// Keep the persisted pullups ID for continuity with existing completion history.
internal val legacyExercises = listOf(
    Exercise("pushups", "Push-ups", "5–10 reps", UnitType.REPS, "Start in a strong plank. Lower your chest with control, then press back to the starting position."),
    Exercise("squats", "Squats", "10–15 reps", UnitType.REPS, "Stand comfortably, sit your hips down and back, then stand tall again."),
    Exercise("deep_squat", "Deep squat hold", "30 seconds", UnitType.SECONDS, "Sink into a comfortable deep squat and hold the bottom position while keeping your feet planted."),
    Exercise("pullups", "Chin-ups", "1–2 reps", UnitType.REPS, "Grip the bar with palms facing you. Pull until your chin clears the bar, then lower under control to a full hang."),
    Exercise("dead_hang", "Dead hang", "30 seconds", UnitType.SECONDS, "Hang from the bar with straight arms and let your body settle into a comfortable hanging position."),
    Exercise("reverse_lunges", "Reverse lunges", "5 reps per side", UnitType.REPS_PER_SIDE, "Step one foot backward, lower into a lunge, then drive through the front foot to return to standing."),
    Exercise("ring_rows", "Ring rows", "5–10 reps", UnitType.REPS, "Hold the rings and lean back with straight arms, keeping your body straight and heels planted. Pull the rings toward your chest, then lower under control."),
    Exercise("hip_flexor", "Hip-flexor stretch", "30 seconds per side", UnitType.SECONDS_PER_SIDE, "From a half-kneeling position, gently move your hips forward until you feel a stretch at the front of the trailing hip.")
)

// Retired movements remain available for displaying saved history in its original units.
val exercises = listOf(
    legacyExercises.first { it.id == "pushups" }.copy(amounts = (5..10).toList()),
    legacyExercises.first { it.id == "squats" }.copy(amounts = (10..15).toList()),
    Exercise(
        "three_point_bridge", "Three-point bridge", "3–5 reps per side", UnitType.REPS_PER_SIDE,
        "Sit with knees bent, feet flat and one hand planted behind you. Press through both feet and the supporting hand to lift your hips, gently turn your chest upward and reach your free arm overhead. Lower with control, then repeat on the other side. Use a comfortable, pain-free range.",
        (3..5).toList()
    ),
    legacyExercises.first { it.id == "pullups" }.copy(amounts = (1..2).toList()),
    Exercise(
        "single_leg_rdl", "Supported single-leg Romanian deadlift", "5–8 reps per side", UnitType.REPS_PER_SIDE,
        "Rest one hand on a sturdy support. Keep a soft bend in your standing knee, send your hips back and extend your other leg behind you as your torso tips forward. Keep your back long and hips level, then return to standing. Repeat on both sides.",
        (5..8).toList()
    ),
    Exercise(
        "tuck_l_sit", "Tuck L-sit", "5–15 seconds", UnitType.SECONDS,
        "Grip stable parallel bars beside your hips. Straighten your arms and press your shoulders down, then lift your feet and draw both knees toward your chest. Hold without swinging and lower with control. Keep one or both feet lightly supported if needed.",
        listOf(5, 10, 15)
    ),
    legacyExercises.first { it.id == "reverse_lunges" }.copy(amounts = listOf(5)),
    legacyExercises.first { it.id == "ring_rows" }.copy(amounts = (5..10).toList()),
    Exercise(
        "ring_hamstring_curls", "Ring hamstring curls", "5–8 reps", UnitType.REPS,
        "Set the rings low and lie on your back with one heel supported in each ring. Press your arms into the floor and lift your hips. Bend your knees to draw your heels toward you, then slowly extend your legs while keeping your hips lifted. Use a shorter range if needed.",
        (5..8).toList()
    ),
    Exercise(
        "pike_pushups", "Pike push-ups", "3–6 reps", UnitType.REPS,
        "Place hands and feet on the floor and raise your hips into an inverted V. Bend your elbows to lower your head toward the floor slightly ahead of your hands, then press back up. Keep your hips high and use a comfortable range; soften your knees if needed.",
        (3..6).toList()
    )
)

fun exerciseForId(id: String): Exercise? =
    exercises.firstOrNull { it.id == id } ?: legacyExercises.firstOrNull { it.id == id }

internal fun exercisesForRoutine(version: Int): List<Exercise> = when (version) {
    1 -> legacyExercises
    CURRENT_ROUTINE_VERSION -> exercises
    else -> emptyList()
}

internal fun nextExerciseIndex(exerciseId: String): Int {
    val nextId = when (exerciseId) {
        "deep_squat" -> "pullups"
        "dead_hang" -> "single_leg_rdl"
        "hip_flexor" -> "pushups"
        else -> null
    }
    return if (nextId != null) exercises.indexOfFirst { it.id == nextId }
    else (exercises.indexOfFirst { it.id == exerciseId } + 1) % exercises.size
}
