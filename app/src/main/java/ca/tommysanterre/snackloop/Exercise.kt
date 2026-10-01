package ca.tommysanterre.snackloop

enum class UnitType { REPS, SECONDS, REPS_PER_SIDE, SECONDS_PER_SIDE }

data class Exercise(
    val id: String,
    val name: String,
    val target: String,
    val unit: UnitType,
    val description: String
)

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
val exercises = listOf(
    Exercise("pushups", "Push-ups", "5–10 reps", UnitType.REPS, "Start in a strong plank. Lower your chest with control, then press back to the starting position."),
    Exercise("squats", "Squats", "10–15 reps", UnitType.REPS, "Stand comfortably, sit your hips down and back, then stand tall again."),
    Exercise("deep_squat", "Deep squat hold", "30 seconds", UnitType.SECONDS, "Sink into a comfortable deep squat and hold the bottom position while keeping your feet planted."),
    Exercise("pullups", "Chin-ups", "1–2 reps", UnitType.REPS, "Grip the bar with palms facing you. Pull until your chin clears the bar, then lower under control to a full hang."),
    Exercise("dead_hang", "Dead hang", "30 seconds", UnitType.SECONDS, "Hang from the bar with straight arms and let your body settle into a comfortable hanging position."),
    Exercise("reverse_lunges", "Reverse lunges", "5 reps per side", UnitType.REPS_PER_SIDE, "Step one foot backward, lower into a lunge, then drive through the front foot to return to standing."),
    Exercise("ring_rows", "Ring rows", "5–10 reps", UnitType.REPS, "Hold the rings and lean back with straight arms, keeping your body straight and heels planted. Pull the rings toward your chest, then lower under control."),
    Exercise("hip_flexor", "Hip-flexor stretch", "30 seconds per side", UnitType.SECONDS_PER_SIDE, "From a half-kneeling position, gently move your hips forward until you feel a stretch at the front of the trailing hip.")
)
