package com.slpolice.reporting.util

import java.util.Calendar
import java.util.Locale

/** Details decoded from a Sri Lankan National Identity Card number. */
data class NicProfile(
    val birthYear: Int,
    val dayOfYear: Int,
    val gender: String,
    val formattedBirthDate: String
)

/**
 * Field-level validation for registration and report filing.
 * Every rule matches the Sri Lankan formats the platform accepts.
 */
object Validators {

    private val OLD_NIC = Regex("^[0-9]{9}[vVxX]$")
    private val NEW_NIC = Regex("^[0-9]{12}$")
    private val SL_MOBILE = Regex("^\\+947[0-9]{8}$")
    private val SL_LANDLINE = Regex("^\\+94(?:1[1-9]|2[1-7]|3[1-8]|4[1-7]|5[1-7]|6[1-3]|8[1-2]|9[1-2])[0-9]{7}$")

    /** Evidence and incidents older than this are refused. */
    const val REPORTING_WINDOW_DAYS = 7
    const val REPORTING_WINDOW_MS = REPORTING_WINDOW_DAYS * 24L * 60L * 60L * 1000L
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val VEHICLE = Regex("^[A-Za-z]{1,3}[- ]?[0-9]{4}$")

    fun nicError(raw: String): String? {
        val nic = raw.trim()
        if (nic.isEmpty()) return "Enter your NIC number"
        if (!OLD_NIC.matches(nic) && !NEW_NIC.matches(nic)) {
            return "Use 9 digits plus V, or the 12-digit format"
        }
        return if (decodeNic(nic) == null) "This NIC number is not valid" else null
    }

    fun nameError(raw: String): String? = when {
        raw.trim().length < 3 -> "Enter your full name as printed on your NIC"
        raw.trim().any { it.isDigit() } -> "A name cannot contain numbers"
        else -> null
    }

    /**
     * Only Sri Lankan telephone numbers are accepted. A local 0-prefixed number is treated as
     * +94, and any other country code is refused outright — the platform is for incidents inside
     * Sri Lanka, reported by people the department can actually reach.
     */
    fun phoneError(raw: String): String? {
        val entered = raw.trim().replace(" ", "").replace("-", "")
        if (entered.isEmpty()) return "Enter your telephone number"

        if (entered.startsWith("00")) {
            return if (entered.startsWith("0094")) {
                phoneError("+" + entered.substring(2))
            } else {
                "Only Sri Lankan numbers are accepted. Your number must begin with +94."
            }
        }
        if (entered.startsWith("+") && !entered.startsWith("+94")) {
            return "Only Sri Lankan numbers are accepted. Your number must begin with +94."
        }

        val canonical = normalisePhone(entered)
        if (!canonical.startsWith("+94")) {
            return "Only Sri Lankan numbers are accepted. Your number must begin with +94."
        }
        return when {
            SL_MOBILE.matches(canonical) || SL_LANDLINE.matches(canonical) -> null
            canonical.length != 12 -> "A Sri Lankan number has 9 digits after +94"
            else -> "That is not a recognised Sri Lankan dialling code"
        }
    }

    fun emailError(raw: String): String? = when {
        raw.isBlank() -> "Enter your email address"
        !EMAIL.matches(raw.trim()) -> "This email address is not valid"
        else -> null
    }

    fun passwordError(raw: String): String? = when {
        raw.length < 8 -> "Use at least 8 characters"
        raw.none { it.isDigit() } -> "Include at least one number"
        raw.none { it.isUpperCase() } -> "Include at least one capital letter"
        raw.none { !it.isLetterOrDigit() } -> "Include at least one symbol"
        else -> null
    }

    fun vehicleError(raw: String): String? =
        if (raw.isBlank() || VEHICLE.matches(raw.trim())) null else "Use a format like CAB-1234"

    /** Stores every number in the single canonical form +94XXXXXXXXX. */
    fun normalisePhone(raw: String): String {
        val phone = raw.trim().replace(" ", "").replace("-", "")
        return when {
            phone.startsWith("+94") -> phone
            phone.startsWith("0094") -> "+94" + phone.substring(4)
            phone.startsWith("94") && phone.length == 11 -> "+" + phone
            phone.startsWith("0") -> "+94" + phone.substring(1)
            else -> phone
        }
    }

    /** True when a moment falls inside the reporting window that ends now. */
    fun withinReportingWindow(millis: Long): Boolean {
        val now = System.currentTimeMillis()
        return millis in (now - REPORTING_WINDOW_MS)..(now + 60_000L)
    }

    fun windowOpensAt(): Long = System.currentTimeMillis() - REPORTING_WINDOW_MS

    fun normaliseNic(raw: String): String = raw.trim().uppercase(Locale.ROOT)

    /**
     * Reads the birth date and gender encoded inside a NIC number. Registration uses this to
     * confirm the holder is an adult, which is what blocks throwaway accounts.
     */
    fun decodeNic(raw: String): NicProfile? {
        val nic = raw.trim().uppercase(Locale.ROOT)
        val year: Int
        var days: Int
        when {
            OLD_NIC.matches(nic) -> {
                year = 1900 + nic.substring(0, 2).toInt()
                days = nic.substring(2, 5).toInt()
            }
            NEW_NIC.matches(nic) -> {
                year = nic.substring(0, 4).toInt()
                days = nic.substring(4, 7).toInt()
            }
            else -> return null
        }

        val gender = if (days > 500) "Female" else "Male"
        if (days > 500) days -= 500
        if (days < 1 || days > 366) return null
        if (year < 1900 || year > Calendar.getInstance().get(Calendar.YEAR)) return null

        val calendar = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.DAY_OF_YEAR, days.coerceAtMost(getActualMaximum(Calendar.DAY_OF_YEAR)))
        }
        return NicProfile(
            birthYear = year,
            dayOfYear = days,
            gender = gender,
            formattedBirthDate = Formatters.date(calendar.timeInMillis)
        )
    }

    fun isAdult(nic: String): Boolean {
        val profile = decodeNic(nic) ?: return false
        return Calendar.getInstance().get(Calendar.YEAR) - profile.birthYear >= 18
    }
}
