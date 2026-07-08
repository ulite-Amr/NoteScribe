package com.uliteamr.notescribe.presentation.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val PATTERN_DATE = "MMM dd, yyyy"
private const val PATTERN_NOTE_DATE = "MMM dd, yyyy · hh:mm a"
private const val PATTERN_TIME = "hh:mm a"

private const val MILLIS_PER_DAY = 86_400_000L
private const val MILLIS_PER_HOUR = 3_600_000L
private const val MILLIS_PER_MINUTE = 60_000L

private val noteFormatter = ThreadLocal.withInitial { SimpleDateFormat(PATTERN_NOTE_DATE, Locale.getDefault()) }
private val dateFormatter = ThreadLocal.withInitial { SimpleDateFormat(PATTERN_DATE, Locale.getDefault()) }
private val timeFormatter = ThreadLocal.withInitial { SimpleDateFormat(PATTERN_TIME, Locale.getDefault()) }

fun formatDate(timestamp: Long): String = dateFormatter.get().format(Date(timestamp))

fun formatTime(timestamp: Long): String = timeFormatter.get().format(Date(timestamp))

fun formatNoteDate(timestamp: Long): String = noteFormatter.get().format(Date(timestamp))

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
