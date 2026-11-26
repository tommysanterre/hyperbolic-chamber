package ca.tommysanterre.snackloop

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryExportTest {
    private val zone = ZoneId.of("America/Toronto")
    private val now = ZonedDateTime.of(2026, 11, 26, 12, 0, 0, 0, zone)

    @Test
    fun `summary excludes skips from averages and preserves per-side units`() {
        val report = historyExport(listOf(
            entry(1, "scapular_pullups", 3),
            entry(2, "scapular_pullups", 0),
            entry(3, "scapular_pullups", 5),
            entry(4, "hip_flexor", 25)
        ), now)

        assertTrue(report.contains("Completed movements: 3. Skips: 1. Days with a completed movement: 1."))
        assertTrue(report.contains("| Scapular pull-ups | reps | 2 | 1 | 4.0 | 3–5 |"))
        assertTrue(report.contains("2026-11-26: 3; 2026-11-26: skip; 2026-11-26: 5"))
        assertTrue(report.contains("| Hip-flexor stretch | seconds per side | 1 | 0 | 25.0 | 25–25 |"))
        assertTrue(report.contains("| Squats | reps | 0 | 0 | — | — | — |"))
    }

    @Test
    fun `recent window uses workout days and retains older records in full log`() {
        val beforeFour = now.withHour(3)
        // Before 4 AM on Nov 26, the 28-day window is Oct 29 through Nov 25.
        val oldest = beforeFour.withMonth(10).withDayOfMonth(29).withHour(3).withMinute(59)
        val included = oldest.withHour(4).withMinute(0)
        val boundary = now.withHour(4).withMinute(0)
        val report = historyExport(listOf(
            entry(1, "scapular_pullups", 1, oldest),
            entry(2, "scapular_pullups", 3, included),
            entry(3, "scapular_pullups", 5, beforeFour.minusMinutes(1)),
            entry(4, "scapular_pullups", 4, boundary)
        ), beforeFour)

        assertTrue(report.contains("2026-10-29 through 2026-11-25 (28 workout days)"))
        assertTrue(report.contains("| Scapular pull-ups | reps | 2 | 0 | 4.0 | 3–5 |"))
        assertTrue(report.contains("| 1 | 2026-10-29T03:59:00-04:00 | 2026-10-28 |"))
        assertTrue(report.contains("| 4 | 2026-11-26T04:00:00-05:00 | 2026-11-26 |"))
    }

    @Test
    fun `full log preserves historical names units versions and deterministic chronology`() {
        val report = historyExport(listOf(
            entry(4, "band_pull_aparts", 12).copy(routineVersion = 5),
            entry(3, "ring_rows", 6).copy(routineVersion = 4),
            entry(2, "pullups", 1).copy(routineVersion = 3),
            entry(1, "pike_pushups", 0, now.minusDays(30)).copy(routineVersion = 2)
        ), now)

        assertTrue(report.contains("| Ring rows | reps | 1 | 0 | 6.0 | 6–6 |"))
        assertTrue(report.contains("| Chin-ups | reps | 1 | 0 | 1.0 | 1–1 |"))
        assertTrue(report.contains("| Chin-ups | pullups | 1 | rep | Completed | 3 |"))
        assertTrue(report.contains("| Pike push-ups | pike_pushups | 0 | reps | Skipped | 2 |"))
        assertTrue(report.contains("| Ring rows | ring_rows | 6 | reps | Completed | 4 |"))
        assertTrue(report.contains("| Band pull-aparts | band_pull_aparts | 12 | reps | Completed | 5 |"))
        val log = report.substringAfter("## Complete saved log")
        assertTrue(log.indexOf("| 1 |") < log.indexOf("| 2 |"))
        assertTrue(log.indexOf("| 2 |") < log.indexOf("| 3 |"))
        assertTrue(log.indexOf("| 3 |") < log.indexOf("| 4 |"))
        assertTrue(report.contains("Saved records: 4."))
    }

    @Test
    fun `unknown movements remain exportable without breaking table rows`() {
        val report = historyExport(listOf(entry(1, "unknown|movement\nname", 7).copy(routineVersion = 99)), now)

        assertTrue(report.contains("unknown\\|movement name"))
        assertTrue(report.contains("| 7 | units | Completed | 99 |"))
        assertFalse(report.contains("unknown|movement\nname"))
    }

    @Test
    fun `report includes routine targets recording limits and missing effort context even without history`() {
        val report = historyExport(emptyList(), now)

        assertTrue(report.contains("| Dead hang | 20–30 seconds | 5, 10, 15, 20, 25, 30 seconds; 0 = skip |"))
        assertTrue(report.contains("| Band pull-aparts | 10–15 reps | 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 reps; 0 = skip |"))
        assertTrue(report.contains("| Scapular pull-ups | 3–5 reps | 1, 2, 3, 4, 5, 6, 7, 8 reps; 0 = skip |"))
        assertTrue(report.contains("| Bear plank hold | 10–15 seconds |"))
        assertTrue(report.contains("how many more clean reps or seconds"))
        assertTrue(report.contains("any assistance, setup changes or discomfort"))
        assertTrue(report.contains("Per-side amounts are recorded per side, not combined."))
        assertTrue(report.contains("No saved movements yet."))
    }

    private fun entry(id: Long, exerciseId: String, amount: Int, at: ZonedDateTime = now) = Completion(
        id = id,
        exerciseId = exerciseId,
        amount = amount,
        completedAt = at.toInstant().toEpochMilli()
    )
}
