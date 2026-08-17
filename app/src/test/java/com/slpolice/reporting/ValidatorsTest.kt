package com.slpolice.reporting

import com.slpolice.reporting.util.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the rules that decide who is allowed to file evidence. */
class ValidatorsTest {

    @Test
    fun `old format NIC is accepted`() {
        assertNull(Validators.nicError("901234567V"))
    }

    @Test
    fun `new format NIC is accepted`() {
        assertNull(Validators.nicError("199012345678"))
    }

    @Test
    fun `NIC with the wrong length is rejected`() {
        assertNotNull(Validators.nicError("12345"))
        assertNotNull(Validators.nicError("9012345678"))
        assertNotNull(Validators.nicError(""))
    }

    @Test
    fun `NIC day of year outside the calendar is rejected`() {
        // 900 maps to day 400 once the female offset is removed, which no year has.
        assertNotNull(Validators.nicError("199090012345"))
    }

    @Test
    fun `gender is read from the day of year offset`() {
        assertEquals("Male", Validators.decodeNic("199012345678")?.gender)
        assertEquals("Female", Validators.decodeNic("199062345678")?.gender)
    }

    @Test
    fun `birth year is read from both NIC formats`() {
        assertEquals(1990, Validators.decodeNic("901234567V")?.birthYear)
        assertEquals(2001, Validators.decodeNic("200112345678")?.birthYear)
    }

    @Test
    fun `reporters under eighteen are blocked`() {
        assertTrue(Validators.isAdult("199012345678"))
        assertFalse(Validators.isAdult("202012345678"))
    }

    @Test
    fun `Sri Lankan mobile and landline numbers are accepted`() {
        assertNull(Validators.phoneError("0771234567"))
        assertNull(Validators.phoneError("+94771234567"))
        assertNull(Validators.phoneError("+94 77 123 4567"))
        assertNull(Validators.phoneError("0112421111"))
    }

    @Test
    fun `numbers from other countries are refused`() {
        assertNotNull(Validators.phoneError("+441234567890"))
        assertNotNull(Validators.phoneError("+11234567890"))
        assertNotNull(Validators.phoneError("+919812345678"))
        assertNotNull(Validators.phoneError("0044123456789"))
        assertNotNull(Validators.phoneError("12345"))
    }

    @Test
    fun `passwords need a capital a number and a symbol`() {
        assertNull(Validators.passwordError("Report@2026"))
        assertNotNull(Validators.passwordError("Ab1@"))
        assertNotNull(Validators.passwordError("nocapital1@"))
        assertNotNull(Validators.passwordError("NoNumber@x"))
        assertNotNull(Validators.passwordError("NoSymbol123"))
    }

    @Test
    fun `vehicle numbers follow the plate format`() {
        assertNull(Validators.vehicleError("CAB-1234"))
        assertNull(Validators.vehicleError("WP 5678"))
        assertNull(Validators.vehicleError(""))
        assertNotNull(Validators.vehicleError("12-CAB"))
    }

    @Test
    fun `phone numbers are stored in canonical plus 94 form`() {
        assertEquals("+94771234567", Validators.normalisePhone("+94771234567"))
        assertEquals("+94771234567", Validators.normalisePhone(" 0771234567 "))
        assertEquals("+94771234567", Validators.normalisePhone("0094771234567"))
        assertEquals("+94771234567", Validators.normalisePhone("077-123-4567"))
    }

    @Test
    fun `the reporting window covers the last seven days`() {
        val now = System.currentTimeMillis()
        val day = 24L * 60L * 60L * 1000L
        assertTrue(Validators.withinReportingWindow(now - day))
        assertTrue(Validators.withinReportingWindow(now - 6 * day))
        assertFalse(Validators.withinReportingWindow(now - 8 * day))
        assertFalse(Validators.withinReportingWindow(now + 2 * day))
    }
}
