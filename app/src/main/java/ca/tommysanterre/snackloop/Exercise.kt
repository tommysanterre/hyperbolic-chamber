package ca.tommysanterre.snackloop

import androidx.annotation.DrawableRes

enum class UnitType { REPS, SECONDS, REPS_PER_SIDE, SECONDS_PER_SIDE }

data class MovementArtwork(
    @param:DrawableRes @get:DrawableRes val baseDrawable: Int,
    @param:DrawableRes @get:DrawableRes val superSaiyanDrawable: Int = baseDrawable,
    val splitNumerator: Int = 1,
    val splitDenominator: Int = 2
) {
    init {
        require(splitNumerator in 1 until splitDenominator) {
            "Split ratio must lie strictly between 0 and 1: $splitNumerator/$splitDenominator"
        }
    }

    @DrawableRes
    fun drawable(superSaiyan: Boolean): Int =
        if (superSaiyan) superSaiyanDrawable else baseDrawable

    fun splitX(imageWidth: Int): Int = imageWidth * splitNumerator / splitDenominator
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
 * Single source of truth for all published routine versions, their exercise metadata,
 * artwork descriptors, and cross-version queue transitions.
 */
object RoutineCatalog {
    const val CURRENT_VERSION = 5

    private data class RoutineVersion(
        val version: Int,
        val exercises: List<Exercise>,
        val replacements: Map<String, String> = emptyMap(),
        val retiredResumeTargets: Map<String, String> = emptyMap()
    )

    // Keep the persisted pullups ID in earlier versions for continuity with saved completion history.
    private val version1 = RoutineVersion(
        version = 1,
        exercises = listOf(
            Exercise(
                id = "pushups",
                name = "Push-ups",
                target = "5–10 reps",
                unit = UnitType.REPS,
                description = "Start in a strong plank. Lower your chest with control, then press back to the starting position.",
                artwork = MovementArtwork(R.drawable.movement_pushups, R.drawable.movement_pushups_ssj)
            ),
            Exercise(
                id = "squats",
                name = "Squats",
                target = "10–15 reps",
                unit = UnitType.REPS,
                description = "Stand comfortably, sit your hips down and back, then stand tall again.",
                artwork = MovementArtwork(R.drawable.movement_squats, R.drawable.movement_squats_ssj)
            ),
            Exercise(
                id = "deep_squat",
                name = "Deep squat hold",
                target = "30 seconds",
                unit = UnitType.SECONDS,
                description = "Sink into a comfortable deep squat and hold the bottom position while keeping your feet planted.",
                artwork = MovementArtwork(R.drawable.movement_deep_squat, R.drawable.movement_deep_squat_ssj)
            ),
            Exercise(
                id = "pullups",
                name = "Chin-ups",
                target = "1–2 reps",
                unit = UnitType.REPS,
                description = "Grip the bar with palms facing you. Pull until your chin clears the bar, then lower under control to a full hang.",
                artwork = MovementArtwork(R.drawable.movement_pullups, R.drawable.movement_pullups_ssj)
            ),
            Exercise(
                id = "dead_hang",
                name = "Dead hang",
                target = "30 seconds",
                unit = UnitType.SECONDS,
                description = "Hang from the bar with straight arms and let your body settle into a comfortable hanging position.",
                artwork = MovementArtwork(R.drawable.movement_dead_hang, R.drawable.movement_dead_hang_ssj)
            ),
            Exercise(
                id = "reverse_lunges",
                name = "Reverse lunges",
                target = "5 reps per side",
                unit = UnitType.REPS_PER_SIDE,
                description = "Step one foot backward, lower into a lunge, then drive through the front foot to return to standing.",
                artwork = MovementArtwork(R.drawable.movement_reverse_lunges, R.drawable.movement_reverse_lunges_ssj)
            ),
            Exercise(
                id = "ring_rows",
                name = "Ring rows",
                target = "5–10 reps",
                unit = UnitType.REPS,
                description = "Hold the rings and lean back with straight arms, keeping your body straight and heels planted. Pull the rings toward your chest, then lower under control.",
                artwork = MovementArtwork(R.drawable.movement_ring_rows, R.drawable.movement_ring_rows_ssj)
            ),
            Exercise(
                id = "hip_flexor",
                name = "Hip-flexor stretch",
                target = "30 seconds per side",
                unit = UnitType.SECONDS_PER_SIDE,
                description = "From a half-kneeling position, gently move your hips forward until you feel a stretch at the front of the trailing hip.",
                artwork = MovementArtwork(R.drawable.movement_hip_flexor)
            )
        )
    )

    // Preserve each published routine so saved names, units and earned loops stay intact.
    private val version2 = RoutineVersion(
        version = 2,
        exercises = listOf(
            version1.exercise("pushups").copy(amounts = (5..10).toList()),
            version1.exercise("squats").copy(amounts = (10..15).toList()),
            Exercise(
                id = "three_point_bridge",
                name = "Three-point bridge",
                target = "3–5 reps per side",
                unit = UnitType.REPS_PER_SIDE,
                description = "Sit with knees bent, feet flat and one hand planted behind you. Press through both feet and the supporting hand to lift your hips, gently turn your chest upward and reach your free arm overhead. Lower with control, then repeat on the other side. Use a comfortable, pain-free range.",
                artwork = MovementArtwork(R.drawable.movement_three_point_bridge, R.drawable.movement_three_point_bridge_ssj),
                amounts = (3..5).toList()
            ),
            version1.exercise("pullups").copy(amounts = (1..2).toList()),
            Exercise(
                id = "single_leg_rdl",
                name = "Supported single-leg Romanian deadlift",
                target = "5–8 reps per side",
                unit = UnitType.REPS_PER_SIDE,
                description = "Rest one hand on a sturdy support. Keep a soft bend in your standing knee, send your hips back and extend your other leg behind you as your torso tips forward. Keep your back long and hips level, then return to standing. Repeat on both sides.",
                // The deadlift illustration has a narrower start panel in both character forms.
                artwork = MovementArtwork(
                    baseDrawable = R.drawable.movement_single_leg_rdl,
                    superSaiyanDrawable = R.drawable.movement_single_leg_rdl_ssj,
                    splitNumerator = 11,
                    splitDenominator = 24
                ),
                amounts = (5..8).toList()
            ),
            Exercise(
                id = "tuck_l_sit",
                name = "Tuck L-sit",
                target = "5–15 seconds",
                unit = UnitType.SECONDS,
                description = "Grip stable parallel bars beside your hips. Straighten your arms and press your shoulders down, then lift your feet and draw both knees toward your chest. Hold without swinging and lower with control. Keep one or both feet lightly supported if needed.",
                artwork = MovementArtwork(R.drawable.movement_tuck_l_sit, R.drawable.movement_tuck_l_sit_ssj),
                amounts = listOf(5, 10, 15)
            ),
            version1.exercise("reverse_lunges").copy(amounts = listOf(5)),
            version1.exercise("ring_rows").copy(amounts = (5..10).toList()),
            Exercise(
                id = "ring_hamstring_curls",
                name = "Ring hamstring curls",
                target = "5–8 reps",
                unit = UnitType.REPS,
                description = "Set the rings low and lie on your back with one heel supported in each ring. Press your arms into the floor and lift your hips. Bend your knees to draw your heels toward you, then slowly extend your legs while keeping your hips lifted. Use a shorter range if needed.",
                artwork = MovementArtwork(R.drawable.movement_ring_hamstring_curls, R.drawable.movement_ring_hamstring_curls_ssj),
                amounts = (5..8).toList()
            ),
            Exercise(
                id = "pike_pushups",
                name = "Pike push-ups",
                target = "3–6 reps",
                unit = UnitType.REPS,
                description = "Place hands and feet on the floor and raise your hips into an inverted V. Bend your elbows to lower your head toward the floor slightly ahead of your hands, then press back up. Keep your hips high and use a comfortable range; soften your knees if needed.",
                artwork = MovementArtwork(R.drawable.movement_pike_pushups, R.drawable.movement_pike_pushups_ssj),
                amounts = (3..6).toList()
            )
        ),
        retiredResumeTargets = mapOf(
            "deep_squat" to "pullups",
            "dead_hang" to "single_leg_rdl",
            "hip_flexor" to "pushups"
        )
    )

    private val version3 = evolveRoutine(
        version = 3,
        previous = version2,
        substitutions = mapOf(
            "pushups" to Exercise(
                id = "knee_pushups",
                name = "Knee push-ups",
                target = "3–8 reps",
                unit = UnitType.REPS,
                description = "Place your hands slightly wider than shoulder-width and rest both knees on the floor. Keep a straight line from shoulders through hips to knees. Lower your chest with elbows angled back, then press up without letting your hips sag. Stop before your form breaks; record however many clean reps you complete.",
                artwork = MovementArtwork(R.drawable.movement_knee_pushups, R.drawable.movement_knee_pushups_ssj),
                amounts = (1..8).toList()
            ),
            "ring_hamstring_curls" to Exercise(
                id = "heel_dig_bridges",
                name = "Heel-dig bridges",
                target = "5–8 reps",
                unit = UnitType.REPS,
                description = "Lie on your back with knees bent and heels on the floor, toes lifted. Keep your arms beside you. Press both heels down and lift your hips until shoulders, hips and knees form a line, without arching your lower back. Lower slowly. Start with heels closer to your hips; move them a little farther away as this becomes easy. No rings or height adjustments needed.",
                artwork = MovementArtwork(R.drawable.movement_heel_dig_bridges, R.drawable.movement_heel_dig_bridges_ssj),
                amounts = (1..8).toList()
            ),
            "pike_pushups" to Exercise(
                id = "bear_plank",
                name = "Bear plank hold",
                target = "5–15 seconds",
                unit = UnitType.SECONDS,
                description = "Start on hands and knees, wrists under shoulders and knees under hips. Tuck your toes, press the floor away and lift your knees just a few centimetres. Keep your back level, hips low and knees bent; no A-frame position needed. Breathe normally and lower your knees before your form breaks. Begin with a short hold.",
                artwork = MovementArtwork(R.drawable.movement_bear_plank, R.drawable.movement_bear_plank_ssj),
                amounts = (1..15).toList()
            )
        )
    )

    private val version4 = evolveRoutine(
        version = 4,
        previous = version3,
        substitutions = mapOf(
            "pullups" to Exercise(
                id = "scapular_pullups",
                name = "Scapular pull-ups",
                target = "3–8 reps",
                unit = UnitType.REPS,
                description = "Grip the bar with palms facing away, about shoulder-width apart. Hang with straight elbows. Keep your elbows straight as you draw your shoulders down away from your ears, lifting your body just a few centimetres. Pause briefly, then return slowly to the relaxed hang without swinging. Keep your feet lightly supported on a stable step if needed. Stop before form breaks and record only clean reps.",
                artwork = MovementArtwork(R.drawable.movement_scapular_pullups, R.drawable.movement_scapular_pullups_ssj),
                amounts = (1..8).toList()
            )
        ),
        updates = mapOf(
            "tuck_l_sit" to { it.copy(amounts = (1..15).toList()) }
        )
    )

    private val version5 = RoutineVersion(
        version = 5,
        exercises = listOf(
            version1.exercise("dead_hang").copy(
                target = "20–30 seconds",
                description = "Hang from the bar with straight arms and let your shoulders settle comfortably without shrugging forcefully. Breathe steadily and step down well before your grip fails.",
                amounts = listOf(5, 10, 15, 20, 25, 30)
            ),
            version1.exercise("deep_squat").copy(
                target = "20–30 seconds",
                description = "Sink into a comfortable deep squat with both feet planted and hold the bottom position while breathing steadily. Keep your chest upright and hold a light support if your heels lift.",
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
            version4.exercise("three_point_bridge"),
            version4.exercise("scapular_pullups").copy(
                target = "3–5 reps",
                description = "Grip the bar with palms facing away, about shoulder-width apart, and hang with straight elbows. Draw your shoulders smoothly down away from your ears to lift your body a few centimetres, pause briefly, then lower slowly without swinging. Keep these sets easy and well short of failure.",
                amounts = (1..8).toList()
            ),
            version1.exercise("hip_flexor").copy(
                target = "20–30 seconds per side",
                description = "From a half-kneeling position with a pad under the trailing knee if desired, gently tuck your pelvis and shift your hips forward until you feel a comfortable stretch at the front of the trailing hip. Stay tall and breathe steadily on each side.",
                artwork = MovementArtwork(R.drawable.movement_hip_flexor, R.drawable.movement_hip_flexor_ssj),
                amounts = listOf(5, 10, 15, 20, 25, 30)
            ),
            version4.exercise("squats").copy(
                target = "10 reps",
                description = "Stand comfortably, sit your hips down and back at an easy pace, then stand tall again. Keep the set smooth and low-fatigue to lubricate your knees and hips.",
                amounts = (5..15).toList()
            ),
            version4.exercise("bear_plank").copy(
                target = "10–15 seconds",
                description = "Start on hands (or low parallettes for neutral wrists) and knees, wrists under shoulders and knees under hips. Tuck your toes, press the floor away, and hover your knees just a few centimetres with a level back. Breathe normally and stop well before form breaks.",
                amounts = (1..15).toList()
            )
        ),
        retiredResumeTargets = mapOf(
            "knee_pushups" to "squats",
            "single_leg_rdl" to "hip_flexor",
            "tuck_l_sit" to "squats",
            "reverse_lunges" to "squats",
            "ring_rows" to "band_pull_aparts",
            "heel_dig_bridges" to "bear_plank"
        )
    )

    private val versions: List<RoutineVersion> = listOf(version1, version2, version3, version4, version5)
    private val versionsByNumber: Map<Int, List<Exercise>> = versions.associate { it.version to it.exercises }
    private val exercisesByVersionAndId: Map<Int, Map<String, Exercise>> =
        versions.associate { routine -> routine.version to routine.exercises.associateBy { it.id } }
    private val latestById: Map<String, Exercise> = buildMap {
        versions.forEach { routine ->
            routine.exercises.forEach { exercise -> put(exercise.id, exercise) }
        }
    }
    private val replacements: Map<String, String> = buildMap {
        versions.forEach { putAll(it.replacements) }
    }
    private val retiredResumeTargets: Map<String, String> = buildMap {
        versions.forEach { putAll(it.retiredResumeTargets) }
    }

    val currentExercises: List<Exercise> = version5.exercises
    private val currentIndexById: Map<String, Int> =
        currentExercises.withIndex().associate { (index, exercise) -> exercise.id to index }

    init {
        check(versions.map { it.version } == (1..CURRENT_VERSION).toList()) {
            "Routine versions must be contiguous from 1 to $CURRENT_VERSION"
        }
        versions.forEach { routine ->
            check(routine.exercises.isNotEmpty()) { "Routine version ${routine.version} must not be empty" }
            check(routine.exercises.map { it.id }.distinct().size == routine.exercises.size) {
                "Duplicate exercise ID in routine version ${routine.version}"
            }
            routine.exercises.forEach { exercise ->
                check(exercise.amounts.isNotEmpty() && exercise.amounts.all { it > 0 }) {
                    "Recording amounts for ${exercise.id} in version ${routine.version} must be positive"
                }
                val resolved = resolveNextExerciseId(exercise.id)
                check(resolved != null && resolved in currentIndexById) {
                    "Exercise ${exercise.id} from version ${routine.version} must resolve to a current exercise"
                }
            }
        }
    }

    fun exerciseAt(index: Int): Exercise =
        currentExercises.getOrElse(index) { currentExercises.first() }

    fun exercisesForVersion(version: Int): List<Exercise> =
        versionsByNumber[version].orEmpty()

    fun exerciseForId(id: String): Exercise? = latestById[id]

    fun resolve(exerciseId: String, routineVersion: Int): Exercise? =
        exercisesByVersionAndId[routineVersion]?.get(exerciseId) ?: latestById[exerciseId]

    fun resolve(completion: Completion): Exercise? =
        resolve(completion.exerciseId, completion.routineVersion)

    fun nextExerciseIndex(exerciseId: String): Int =
        resolveNextExerciseId(exerciseId)?.let(currentIndexById::get) ?: 0

    fun formatRecordedAmount(amount: Int, exercise: Exercise?): String =
        if (amount == 0) "Skipped"
        else "$amount ${exercise?.unit?.historyLabel(amount) ?: "units"}"

    private fun resolveNextExerciseId(exerciseId: String): String? {
        val equivalent = followChain(exerciseId, includeRetiredTargets = false)
        currentIndexById[equivalent]?.let { currentIndex ->
            return currentExercises[(currentIndex + 1) % currentExercises.size].id
        }
        retiredResumeTargets[equivalent]?.let { target ->
            val resumeId = followChain(target, includeRetiredTargets = true)
            return resumeId.takeIf { it in currentIndexById }
        }
        return null
    }

    private fun followChain(startId: String, includeRetiredTargets: Boolean): String {
        var current = startId
        val visited = mutableSetOf<String>()
        while (visited.add(current)) {
            if (includeRetiredTargets && current in currentIndexById) break
            val next = replacements[current]
                ?: (if (includeRetiredTargets) retiredResumeTargets[current] else null)
                ?: break
            current = next
        }
        return current
    }

    private fun RoutineVersion.exercise(id: String): Exercise =
        exercises.first { it.id == id }

    private fun evolveRoutine(
        version: Int,
        previous: RoutineVersion,
        substitutions: Map<String, Exercise> = emptyMap(),
        updates: Map<String, (Exercise) -> Exercise> = emptyMap()
    ): RoutineVersion {
        val previousIds = previous.exercises.map { it.id }.toSet()
        substitutions.keys.forEach { oldId ->
            require(oldId in previousIds) { "Cannot substitute unknown exercise '$oldId' in version $version" }
        }
        updates.keys.forEach { id ->
            require(id in previousIds) { "Cannot update unknown exercise '$id' in version $version" }
            require(id !in substitutions) { "Cannot both substitute and update '$id' in version $version" }
        }
        val evolved = previous.exercises.map { exercise ->
            substitutions[exercise.id]
                ?: updates[exercise.id]?.invoke(exercise)
                ?: exercise
        }
        return RoutineVersion(
            version = version,
            exercises = evolved,
            replacements = substitutions.mapValues { (_, replacement) -> replacement.id }
        )
    }
}
