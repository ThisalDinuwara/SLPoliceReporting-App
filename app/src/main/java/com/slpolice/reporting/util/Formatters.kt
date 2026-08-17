package com.slpolice.reporting.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object Formatters {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.ENGLISH)
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH)

    fun date(millis: Long): String = dateFormat.format(Date(millis))

    fun dateTime(millis: Long): String = dateTimeFormat.format(Date(millis))

    fun time(millis: Long): String = timeFormat.format(Date(millis))

    fun relative(millis: Long): String {
        val diff = System.currentTimeMillis() - millis
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours h ago"
            days < 7 -> "$days d ago"
            else -> date(millis)
        }
    }

    fun fileSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.0f KB".format(bytes / 1024.0)
        else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    }

    fun shortHash(hash: String): String =
        if (hash.length <= 16) hash else hash.substring(0, 8) + "\u2026" + hash.takeLast(8)
}
