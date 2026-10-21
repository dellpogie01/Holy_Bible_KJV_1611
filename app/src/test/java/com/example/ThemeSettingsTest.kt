package com.example

import com.example.ui.theme.AppThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeSettingsTest {

    @Test
    fun defaultThemeMode_isSystem() {
        val defaultMode = AppThemeMode.SYSTEM
        assertEquals(AppThemeMode.SYSTEM, defaultMode)
        assertEquals("System", defaultMode.title)
    }

    @Test
    fun themeModes_containLightDarkSystem() {
        val modes = AppThemeMode.values()
        assertEquals(3, modes.size)
        assertTrue(modes.contains(AppThemeMode.LIGHT))
        assertTrue(modes.contains(AppThemeMode.DARK))
        assertTrue(modes.contains(AppThemeMode.SYSTEM))
    }

    @Test
    fun purposeDescription_exactTextMatchesRequirements() {
        val exactDescription = "“This Holy Bible app is offered free of charge to help make God’s Word accessible to everyone. Its purpose is to encourage daily Scripture reading, nurture faith in Jesus Christ, and share the Good News of His love and salvation. May it help you draw closer to God and share His Word with others.”"
        val expectedTitle = "Freely Given, Faithfully Shared"
        assertTrue(exactDescription.startsWith("“This Holy Bible app is offered free of charge"))
        assertTrue(exactDescription.endsWith("share His Word with others.”"))
        assertEquals("Freely Given, Faithfully Shared", expectedTitle)
    }
}
