package ca.tommysanterre.snackloop

data class StrengthExercise(
    val id: String,
    val slotLabel: String,
    val name: String,
    val target: String,
    val targetTop: Int,
    val unit: UnitType,
    val description: String,
    val artwork: MovementArtwork?,
    val amounts: List<Int>,
    val settings: List<String>
) {
    val defaultSetting: String get() = settings.first()

    fun recordingAmounts(): List<Int> = listOf(0) + amounts

    fun recordingActionDescription(amount: Int): String =
        if (amount == 0) "Skip $name set and move to next set"
        else "Record $amount ${unit.historyLabel(amount)} for $name and move to next set"

    fun nextSetting(currentSetting: String): String? {
        val index = settings.indexOf(currentSetting)
        return if (index in 0 until settings.lastIndex) settings[index + 1] else null
    }
}

data class SupersetPair(
    val label: String,
    val first: StrengthExercise,
    val second: StrengthExercise,
    val rounds: Int = 3,
    val restAfterFirstSeconds: Int = 45,
    val restAfterSecondSeconds: Int = 60
)

data class StrengthTemplate(
    val id: String,
    val name: String,
    val subtitle: String,
    val warmupItems: List<String>,
    val pairs: List<SupersetPair>,
    val cooldownItems: List<String>
) {
    val exercises: List<StrengthExercise> = pairs.flatMap { listOf(it.first, it.second) }

    val totalWorkingSets: Int = pairs.sumOf { it.rounds * 2 }
}

/**
 * Active 2x30-minute full-body strength program (owned gear only),
 * mirroring docs/training-plan.md.
 */
object StrengthCatalog {
    const val STRENGTH_A_ID = "strength_a"
    const val STRENGTH_B_ID = "strength_b"

    val strengthA = StrengthTemplate(
        id = STRENGTH_A_ID,
        name = "Strength A",
        subtitle = "Squat · Push · Row · Hinge · Posture · Core",
        warmupItems = listOf(
            "Arm circles & light hip hinges",
            "5 easy bodyweight squats",
            "10 light band pull-aparts (DYNASQUARE Red, single strand)",
            "3–5 easy scapular pull-ups",
            "1 easy ramp-up set of Pair A"
        ),
        pairs = listOf(
            SupersetPair(
                label = "Pair A",
                first = StrengthExercise(
                    id = "strength_tempo_squat",
                    slotLabel = "A1",
                    name = "Tempo Bodyweight Squat",
                    target = "3 × 8–12",
                    targetTop = 12,
                    unit = UnitType.REPS,
                    description = "Lower for a controlled 3 seconds to pain-free depth with a tall torso, then stand smoothly. Stop with 1–2 clean reps in reserve.",
                    artwork = MovementArtwork(R.drawable.movement_squats, R.drawable.movement_squats_ssj),
                    amounts = (5..15).toList(),
                    settings = listOf("3s lower", "3s lower + pause")
                ),
                second = StrengthExercise(
                    id = "strength_incline_pushups",
                    slotLabel = "A2",
                    name = "Incline Push-ups",
                    target = "3 × 6–12",
                    targetTop = 12,
                    unit = UnitType.REPS,
                    description = "Hands on Corength bar (keep force vertical and braced on foam). Hold a straight plank from head to heels, lower chest with control, and press back up.",
                    artwork = null,
                    amounts = (4..15).toList(),
                    settings = listOf("93 cm", "86 cm", "79 cm", "72 cm", "Knee (Bitor)", "Full (Bitor)")
                )
            ),
            SupersetPair(
                label = "Pair B",
                first = StrengthExercise(
                    id = "strength_parallel_bar_rows",
                    slotLabel = "B1",
                    name = "Parallel Bar Inverted Rows",
                    target = "3 × 8–12",
                    targetTop = 12,
                    unit = UnitType.REPS,
                    description = "One hand on each Corength bar with a neutral grip, body between bars. Pull your chest straight up without shrugging; progress from knees bent to straight legs on heels.",
                    artwork = null,
                    amounts = (5..15).toList(),
                    settings = listOf("93 cm bent", "86 cm bent", "93 cm straight", "86 cm straight")
                ),
                second = StrengthExercise(
                    id = "strength_heel_dig_bridges",
                    slotLabel = "B2",
                    name = "Heel-Dig Bridges",
                    target = "3 × 8–12",
                    targetTop = 12,
                    unit = UnitType.REPS,
                    description = "Lie on your back with heels planted and toes up. Drive through your heels to lift hips into a straight line without arching your lower back; walk heels farther out to progress.",
                    artwork = MovementArtwork(R.drawable.movement_heel_dig_bridges, R.drawable.movement_heel_dig_bridges_ssj),
                    amounts = (5..15).toList(),
                    settings = listOf("Heels close", "Heels mid", "Heels far")
                )
            ),
            SupersetPair(
                label = "Pair C",
                first = StrengthExercise(
                    id = "strength_band_pull_aparts",
                    slotLabel = "C1",
                    name = "Band Pull-Aparts",
                    target = "3 × 12–15",
                    targetTop = 15,
                    unit = UnitType.REPS,
                    description = "DYNASQUARE Red single strand held at chest height with straight arms, shoulders down and ribs tucked. Pull hands wide to your chest and return slowly.",
                    artwork = MovementArtwork(R.drawable.movement_band_pull_aparts, R.drawable.movement_band_pull_aparts_ssj),
                    amounts = (8..18).toList(),
                    settings = listOf("Red (wide)", "Red (medium)", "Red (narrow)")
                ),
                second = StrengthExercise(
                    id = "strength_ab_wheel_rollouts",
                    slotLabel = "C2",
                    name = "Kneeling Ab Wheel Rollouts",
                    target = "3 × 5–8",
                    targetTop = 8,
                    unit = UnitType.REPS,
                    description = "Knees on foam pad with ribs tucked and glutes braced. Roll forward only through a short controlled range with zero lower-back sag; swap to Bear Plank Hold if low back feels stressed.",
                    artwork = null,
                    amounts = (3..12).toList(),
                    settings = listOf("Short range", "Medium range", "Full range")
                )
            )
        ),
        cooldownItems = listOf(
            "20–30s relaxed Dead hang"
        )
    )

    val strengthB = StrengthTemplate(
        id = STRENGTH_B_ID,
        name = "Strength B",
        subtitle = "Hinge · Stability · Chin-ups · Lunge · Core",
        warmupItems = listOf(
            "Light hip hinges",
            "10 light band pull-aparts (DYNASQUARE Red, single strand)",
            "5 easy bodyweight squats",
            "1 easy ramp-up set of Pair A"
        ),
        pairs = listOf(
            SupersetPair(
                label = "Pair A",
                first = StrengthExercise(
                    id = "strength_single_leg_rdl",
                    slotLabel = "A1",
                    name = "Supported Single-Leg RDL",
                    target = "3 × 6–10 / side",
                    targetTop = 10,
                    unit = UnitType.REPS_PER_SIDE,
                    description = "One hand lightly on a sturdy support, soft standing knee, hips level, long flat back. Hinge hips backward smoothly, then drive through the standing heel to return.",
                    artwork = MovementArtwork(
                        R.drawable.movement_single_leg_rdl,
                        R.drawable.movement_single_leg_rdl_ssj,
                        splitNumerator = 11,
                        splitDenominator = 24
                    ),
                    amounts = (4..12).toList(),
                    settings = listOf("Light support", "Fingertip support", "Free balance")
                ),
                second = StrengthExercise(
                    id = "strength_bear_plank_taps",
                    slotLabel = "A2",
                    name = "Bear Plank Shoulder Taps",
                    target = "3 × 6–10 / side",
                    targetTop = 10,
                    unit = UnitType.REPS_PER_SIDE,
                    description = "Hands on floor or Bitor parallettes with knees hovering 2–3 cm under hips. Tap the opposite shoulder slowly while keeping your hips completely quiet without rocking.",
                    artwork = null,
                    amounts = (4..12).toList(),
                    settings = listOf("Floor", "Bitor parallettes")
                )
            ),
            SupersetPair(
                label = "Pair B",
                first = StrengthExercise(
                    id = "strength_band_chinups",
                    slotLabel = "B1",
                    name = "Band-Assisted Chin-ups",
                    target = "3 × 4–8",
                    targetTop = 8,
                    unit = UnitType.REPS,
                    description = "GoZone Orange band looped over the pull-up bar (knee or foot in band; stack Red if needed). Palms facing you, pull chin smoothly over the bar, and lower for 2–3 seconds.",
                    artwork = null,
                    amounts = (1..10).toList(),
                    settings = listOf("Orange + Red", "Orange", "Red", "Unassisted")
                ),
                second = StrengthExercise(
                    id = "strength_reverse_lunges",
                    slotLabel = "B2",
                    name = "Bodyweight Reverse Lunges",
                    target = "3 × 6–10 / side",
                    targetTop = 10,
                    unit = UnitType.REPS_PER_SIDE,
                    description = "Step smoothly backward with an upright torso (gentler on knees than forward lunges), gently touch toward a foam pad, and drive through the front heel to stand.",
                    artwork = MovementArtwork(R.drawable.movement_reverse_lunges, R.drawable.movement_reverse_lunges_ssj),
                    amounts = (4..12).toList(),
                    settings = listOf("Bodyweight", "3s lower")
                )
            ),
            SupersetPair(
                label = "Pair C",
                first = StrengthExercise(
                    id = "strength_tuck_l_sit",
                    slotLabel = "C1",
                    name = "Tuck L-Sit",
                    target = "3 × 10–20s",
                    targetTop = 20,
                    unit = UnitType.SECONDS,
                    description = "Tall Corength parallel bars with straight arms, shoulders depressed away from your ears, and knees tucked cleanly. Progress to low Bitor parallettes later.",
                    artwork = MovementArtwork(R.drawable.movement_tuck_l_sit, R.drawable.movement_tuck_l_sit_ssj),
                    amounts = listOf(5, 8, 10, 12, 15, 18, 20, 25),
                    settings = listOf("Corength tall", "Bitor low")
                ),
                second = StrengthExercise(
                    id = "strength_bear_plank_hold",
                    slotLabel = "C2",
                    name = "Bear Plank Hold",
                    target = "3 × 15–25s",
                    targetTop = 25,
                    unit = UnitType.SECONDS,
                    description = "Knees hovering 2–3 cm directly under hips with a level back and steady breathing. Use Bitor parallettes for neutral wrists if preferred.",
                    artwork = MovementArtwork(R.drawable.movement_bear_plank, R.drawable.movement_bear_plank_ssj),
                    amounts = listOf(10, 15, 20, 25, 30),
                    settings = listOf("Floor", "Bitor parallettes")
                )
            )
        ),
        cooldownItems = listOf(
            "20–30s relaxed Dead hang",
            "Brief hip-flexor stretch (20–30s per side)"
        )
    )

    val templates: List<StrengthTemplate> = listOf(strengthA, strengthB)

    private val templateById: Map<String, StrengthTemplate> = templates.associateBy { it.id }
    private val exerciseById: Map<String, StrengthExercise> =
        templates.flatMap { it.exercises }.associateBy { it.id }

    init {
        check(templateById.size == templates.size) { "Duplicate strength template ID" }
        val allExercises = templates.flatMap { it.exercises }
        check(exerciseById.size == allExercises.size) { "Duplicate strength exercise ID" }
        allExercises.forEach { exercise ->
            check(exercise.amounts.isNotEmpty() && exercise.amounts.all { it > 0 }) {
                "Recording amounts for ${exercise.id} must be positive"
            }
            check(exercise.settings.isNotEmpty()) {
                "Progression settings for ${exercise.id} must not be empty"
            }
        }
    }

    fun templateForId(id: String): StrengthTemplate? = templateById[id]

    fun exerciseForId(id: String): StrengthExercise? = exerciseById[id]
}
