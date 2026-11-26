package ca.tommysanterre.snackloop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthLogicTest {
    @Test
    fun `strength templates flatten into 18 alternating superset steps with rest intervals`() {
        StrengthCatalog.templates.forEach { template ->
            val steps = buildStrengthSteps(template)
            assertEquals(18, steps.size)
            assertEquals(template.totalWorkingSets, steps.size)

            // Pair A rounds 1..3 alternate A1 and A2
            assertEquals(template.pairs[0].first.id, steps[0].exercise.id)
            assertEquals(1, steps[0].setNumber)
            assertEquals(45, steps[0].restAfterSeconds)

            assertEquals(template.pairs[0].second.id, steps[1].exercise.id)
            assertEquals(1, steps[1].setNumber)
            assertEquals(60, steps[1].restAfterSeconds)

            assertEquals(template.pairs[0].first.id, steps[2].exercise.id)
            assertEquals(2, steps[2].setNumber)

            // Final set of the session has no trailing rest timer
            assertEquals(0, steps.last().restAfterSeconds)
            assertEquals(3, steps.last().setNumber)
            assertEquals(template.pairs.last().second.id, steps.last().exercise.id)
        }
    }

    @Test
    fun `recommended template alternates between Strength A and Strength B`() {
        assertEquals(StrengthCatalog.strengthA, recommendedNextTemplate(emptyList()))

        val finishedA = StrengthSession(id = 1, templateId = StrengthCatalog.STRENGTH_A_ID, startedAt = 1000L, finishedAt = 2000L)
        assertEquals(StrengthCatalog.strengthB, recommendedNextTemplate(listOf(finishedA)))

        val finishedB = StrengthSession(id = 2, templateId = StrengthCatalog.STRENGTH_B_ID, startedAt = 3000L, finishedAt = 4000L)
        assertEquals(StrengthCatalog.strengthA, recommendedNextTemplate(listOf(finishedA, finishedB)))

        // Unfinished session does not change next recommendation
        val inProgressA = StrengthSession(id = 3, templateId = StrengthCatalog.STRENGTH_A_ID, startedAt = 5000L, finishedAt = null)
        assertEquals(StrengthCatalog.strengthA, recommendedNextTemplate(listOf(finishedA, finishedB, inProgressA)))
    }

    @Test
    fun `active strength session reconstructs step progress, rest timer, and undo cleanly`() {
        val session = StrengthSession(id = 10, templateId = StrengthCatalog.STRENGTH_A_ID, startedAt = 1_000L, finishedAt = null)
        val steps = buildStrengthSteps(StrengthCatalog.strengthA)

        val initial = activeStrengthSessionState(listOf(session), emptyList())!!
        assertEquals(0, initial.nextStepIndex)
        assertFalse(initial.isReadyToFinish)
        assertNull(initial.lastRestSeconds)

        val log0 = StrengthSetLog(id = 1, sessionId = 10, stepIndex = 0, exerciseId = steps[0].exercise.id, setNumber = 1, amount = 10, setting = "3s lower", completedAt = 2_000L)
        val log1 = StrengthSetLog(id = 2, sessionId = 10, stepIndex = 1, exerciseId = steps[1].exercise.id, setNumber = 1, amount = 8, setting = "86 cm", completedAt = 3_000L)

        val afterTwo = activeStrengthSessionState(listOf(session), listOf(log0, log1))!!
        assertEquals(2, afterTwo.nextStepIndex)
        assertEquals(60, afterTwo.lastRestSeconds)
        assertEquals(3_000L, afterTwo.lastCompletedAt)
        assertEquals(2, afterTwo.completedSetsCount)

        // Undo returns cleanly to step 1 with step 0's rest duration
        val afterUndo = activeStrengthSessionState(listOf(session), listOf(log0))!!
        assertEquals(1, afterUndo.nextStepIndex)
        assertEquals(45, afterUndo.lastRestSeconds)

        // All 18 sets marks session ready for cooldown & finish
        val allLogs = steps.mapIndexed { idx, step ->
            StrengthSetLog(
                id = idx.toLong() + 1,
                sessionId = 10,
                stepIndex = idx,
                exerciseId = step.exercise.id,
                setNumber = step.setNumber,
                amount = 10,
                setting = step.exercise.defaultSetting,
                completedAt = 2_000L + idx * 1_000L
            )
        }
        val ready = activeStrengthSessionState(listOf(session), allLogs)!!
        assertTrue(ready.isReadyToFinish)
        assertNull(ready.currentStep)
        assertNull(ready.lastRestSeconds)
    }

    @Test
    fun `double progression triggers only when all 3 sets hit targetTop at the same setting`() {
        val session = StrengthSession(id = 1, templateId = StrengthCatalog.STRENGTH_A_ID, startedAt = 1_000L, finishedAt = 2_000L)
        val pushups = StrengthCatalog.exerciseForId("strength_incline_pushups")!!

        val belowTopLogs = listOf(
            StrengthSetLog(id = 1, sessionId = 1, stepIndex = 1, exerciseId = pushups.id, setNumber = 1, amount = 12, setting = "86 cm"),
            StrengthSetLog(id = 2, sessionId = 1, stepIndex = 3, exerciseId = pushups.id, setNumber = 2, amount = 12, setting = "86 cm"),
            StrengthSetLog(id = 3, sessionId = 1, stepIndex = 5, exerciseId = pushups.id, setNumber = 3, amount = 11, setting = "86 cm")
        )
        val notReady = lastPerformanceByExercise(listOf(session), belowTopLogs)[pushups.id]!!
        assertEquals("86 cm", notReady.setting)
        assertEquals(listOf(12, 12, 11), notReady.amounts)
        assertFalse(notReady.readyToProgress)
        assertNull(notReady.suggestedNextSetting)

        val topLogs = listOf(
            StrengthSetLog(id = 1, sessionId = 1, stepIndex = 1, exerciseId = pushups.id, setNumber = 1, amount = 12, setting = "86 cm"),
            StrengthSetLog(id = 2, sessionId = 1, stepIndex = 3, exerciseId = pushups.id, setNumber = 2, amount = 12, setting = "86 cm"),
            StrengthSetLog(id = 3, sessionId = 1, stepIndex = 5, exerciseId = pushups.id, setNumber = 3, amount = 12, setting = "86 cm")
        )
        val ready = lastPerformanceByExercise(listOf(session), topLogs)[pushups.id]!!
        assertTrue(ready.readyToProgress)
        assertEquals("79 cm", ready.suggestedNextSetting)

        // Setting resolution prefers current session first, then previous finished session, then default
        assertEquals("86 cm", defaultSettingForExercise(pushups, emptyList(), mapOf(pushups.id to ready)))
        val inSessionOverride = listOf(
            StrengthSetLog(id = 4, sessionId = 2, stepIndex = 1, exerciseId = pushups.id, setNumber = 1, amount = 8, setting = "79 cm")
        )
        assertEquals("79 cm", defaultSettingForExercise(pushups, inSessionOverride, mapOf(pushups.id to ready)))
        assertEquals("93 cm", defaultSettingForExercise(pushups, emptyList(), emptyMap()))
    }

    @Test
    fun `strength exercises have valid targets, positive amounts, and distinct non-null artwork transformations`() {
        val allExercises = StrengthCatalog.templates.flatMap { it.exercises }
        assertEquals(12, allExercises.size)
        allExercises.forEach { exercise ->
            assertNotNull(StrengthCatalog.exerciseForId(exercise.id))
            assertTrue(exercise.amounts.isNotEmpty() && exercise.amounts.all { it > 0 })
            assertEquals(0, exercise.recordingAmounts().first())
            exercise.artwork?.let { art ->
                assertTrue(art.drawable(false) != art.drawable(true))
                assertTrue(art.splitX(1536) in 1 until 1536)
            }
        }
    }
}
