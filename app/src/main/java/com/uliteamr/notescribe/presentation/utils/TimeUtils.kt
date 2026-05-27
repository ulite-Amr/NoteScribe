package com.uliteamr.notescribe.presentation.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/** Readable format for standalone dates: "Jan 15, 2025". */
private const val PATTERN_DATE = "MMM dd, yyyy"

/** Readable format for timestamps shown on notes: "Jan 15, 2025 · 02:30 PM". */
private const val PATTERN_NOTE_DATE = "MMM dd, yyyy · hh:mm a"

/** Short time-only format: "02:30 PM". */
private const val PATTERN_TIME = "hh:mm a"

private const val MILLIS_PER_DAY = 86_400_000L
private const val MILLIS_PER_HOUR = 3_600_000L
private const val MILLIS_PER_MINUTE = 60_000L

private val noteFormatter by lazy { SimpleDateFormat(PATTERN_NOTE_DATE, Locale.getDefault()) }
private val dateFormatter by lazy { SimpleDateFormat(PATTERN_DATE, Locale.getDefault()) }
private val timeFormatter by lazy { SimpleDateFormat(PATTERN_TIME, Locale.getDefault()) }

/**
 * Formats [timestamp] as a calendar date string.
 *
 * Example output: `"Jan 15, 2025"`
 */
fun formatDate(timestamp: Long): String = dateFormatter.format(Date(timestamp))

/**
 * Formats [timestamp] as a time-only string.
 *
 * Example output: `"02:30 PM"`
 */
fun formatTime(timestamp: Long): String = timeFormatter.format(Date(timestamp))

/**
 * Formats [timestamp] used on note cards.
 *
 * Example output: `"Jan 15, 2025 · 02:30 PM"`
 */
fun formatNoteDate(timestamp: Long): String = noteFormatter.format(Date(timestamp))

/**
 * Returns a human-readable relative description of [timestamp] compared to now.
 *
 * - Same day: `"Today at 02:30 PM"`
 * - Yesterday: `"Yesterday at 02:30 PM"`
 * - This week: `"Monday at 02:30 PM"`
 * - Older: formatted date (e.g. `"Jan 15, 2025"`)
 */
fun formatRelative(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val targetCal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val nowCal = Calendar.getInstance()

    val timeStr = formatTime(timestamp)

    return when {
        isSameDay(targetCal, nowCal) -> "Today at $timeStr"
        isYesterday(targetCal, nowCal) -> "Yesterday at $timeStr"
        diff < 7 * MILLIS_PER_DAY -> "${targetCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())} at $timeStr"
        else -> formatDate(timestamp)
    }
}

/**
 * Returns a concise human-readable duration from [timestamp] until now.
 *
 * Examples: `"Just now"`, `"5m ago"`, `"3h ago"`, `"2d ago"`, `"Jan 15, 2025"`
 */
fun formatDuration(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < MILLIS_PER_MINUTE -> "Just now"
        diff < MILLIS_PER_HOUR -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
        diff < MILLIS_PER_DAY -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
        diff < 7 * MILLIS_PER_DAY -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
        else -> formatDate(timestamp)
    }
}

private fun isSameDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

private fun isYesterday(target: Calendar, now: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = now.timeInMillis
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return isSameDay(target, yesterday)
}
