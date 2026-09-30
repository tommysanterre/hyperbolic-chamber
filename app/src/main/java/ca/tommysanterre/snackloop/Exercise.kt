package ca.tommysanterre.snackloop

enum class UnitType { REPS, SECONDS, REPS_PER_SIDE, SECONDS_PER_SIDE }

data class Exercise(
    val id: String,
    val name: String,
    val target: String,
    val unit: UnitType,
    val description: String
)

val exercises = listOf(
    Exercise("pushups", "Push-ups", "5–10 reps", UnitType.REPS, "Start in a strong plank. Lower your chest with control, then press back to the starting position."),
    Exercise("squats", "Squats", "10–15 reps", UnitType.REPS, "Stand comfortably, sit your hips down and back, then stand tall again."),
    Exercise("deep_squat", "Deep squat hold", "30 sec", UnitType.SECONDS, "Sink into a comfortable deep squat and hold the bottom position while keeping your feet planted."),
    Exercise("pullups", "Pull-ups", "2–5 reps", UnitType.REPS, "Hang from the bar, pull until your chin clears it, then lower under control to a full hang."),
    Exercise("dead_hang", "Dead hang", "30 sec", UnitType.SECONDS, "Hang from the bar with straight arms and let your body settle into a comfortable hanging position."),
    Exercise("reverse_lunges", "Reverse lunges", "5 / side", UnitType.REPS_PER_SIDE, "Step one foot backward, lower into a lunge, then drive through the front foot to return to standing."),
    Exercise("hip_flexor", "Hip-flexor stretch", "30 sec / side", UnitType.SECONDS_PER_SIDE, "From a half-kneeling position, gently move your hips forward until you feel a stretch at the front of the trailing hip.")
)
