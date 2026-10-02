package ca.tommysanterre.snackloop

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {
    @Test
    fun `prompts only for a newer release`() {
        assertTrue(isNewerVersion("v0.3.6", "0.3.5"))
        assertTrue(isNewerVersion("v0.3.10", "0.3.9"))
        assertFalse(isNewerVersion("v0.3.5", "0.3.5"))
        assertFalse(isNewerVersion("v0.3.4", "0.3.5"))
    }

    @Test
    fun `ignores malformed versions`() {
        assertFalse(isNewerVersion("latest", "0.3.5"))
        assertFalse(isNewerVersion("v0.3.6", "unknown"))
    }
}
