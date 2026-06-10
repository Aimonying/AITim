package com.tingjizhushou.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Time-related utility functions for formatting timestamps and dates.
 */
object TimeUtils {
    
    private const val DEFAULT_DATE_FORMAT = "yyyy-MM-dd"
    private const val DEFAULT_TIME_FORMAT = "HH:mm:ss"
    private const val DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
    private const val COMPACT_DATETIME_FORMAT = "yyyyMMdd_HHmmss"
    
    /**
     * Format timestamp to date string.
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date string (e.g., "2024-01-15")
     */
    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault())
            .format(Date(timestamp))
    }
    
    /**
     * Format timestamp to time string.
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted time string (e.g., "14:30:45")
     */
    fun formatTime(timestamp: Long): String {
        return SimpleDateFormat(DEFAULT_TIME_FORMAT, Locale.getDefault())
            .format(Date(timestamp))
    }
    
    /**
     * Format timestamp to datetime string.
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted datetime string (e.g., "2024-01-15 14:30:45")
     */
    fun formatDateTime(timestamp: Long): String {
        return SimpleDateFormat(DEFAULT_DATETIME_FORMAT, Locale.getDefault())
            .format(Date(timestamp))
    }
    
    /**
     * Format timestamp to compact datetime string.
     * @param timestamp Unix timestamp in milliseconds
     * @return Compact formatted string (e.g., "20240115_143045")
     */
    fun formatCompactDateTime(timestamp: Long): String {
        return SimpleDateFormat(COMPACT_DATETIME_FORMAT, Locale.getDefault())
            .format(Date(timestamp))
    }
    
    /**
     * Format timestamp to custom pattern.
     * @param timestamp Unix timestamp in milliseconds
     * @param pattern Date format pattern
     * @return Formatted string
     */
    fun format(timestamp: Long, pattern: String): String {
        return SimpleDateFormat(pattern, Locale.getDefault())
            .format(Date(timestamp))
    }
    
    /**
     * Get relative time string (e.g., "刚刚", "5分钟前", "2小时前").
     * @param timestamp Unix timestamp in milliseconds
     * @return Relative time string in Chinese
     */
    fun getRelativeTimeString(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "刚刚"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "${minutes}分钟前"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "${hours}小时前"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "${days}天前"
            }
            diff < TimeUnit.DAYS.toMillis(30) -> {
                val weeks = TimeUnit.MILLISECONDS.toDays(diff) / 7
                "${weeks}周前"
            }
            diff < TimeUnit.DAYS.toMillis(365) -> {
                val months = TimeUnit.MILLISECONDS.toDays(diff) / 30
                "${months}个月前"
            }
            else -> {
                val years = TimeUnit.MILLISECONDS.toDays(diff) / 365
                "${years}年前"
            }
        }
    }
    
    /**
     * Get relative time string in English.
     * @param timestamp Unix timestamp in milliseconds
     * @return Relative time string in English
     */
    fun getRelativeTimeStringEn(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes minute${if (minutes > 1) "s" else ""} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours hour${if (hours > 1) "s" else ""} ago"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "$days day${if (days > 1) "s" else ""} ago"
            }
            diff < TimeUnit.DAYS.toMillis(30) -> {
                val weeks = TimeUnit.MILLISECONDS.toDays(diff) / 7
                "$weeks week${if (weeks > 1) "s" else ""} ago"
            }
            diff < TimeUnit.DAYS.toMillis(365) -> {
                val months = TimeUnit.MILLISECONDS.toDays(diff) / 30
                "$months month${if (months > 1) "s" else ""} ago"
            }
            else -> {
                val years = TimeUnit.MILLISECONDS.toDays(diff) / 365
                "$years year${if (years > 1) "s" else ""} ago"
            }
        }
    }
    
    /**
     * Get start of day timestamp.
     * @param timestamp Unix timestamp in milliseconds
     * @return Timestamp at 00:00:00 of the same day
     */
    fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    /**
     * Get end of day timestamp.
     * @param timestamp Unix timestamp in milliseconds
     * @return Timestamp at 23:59:59 of the same day
     */
    fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
    
    /**
     * Check if two timestamps are on the same day.
     * @param timestamp1 First timestamp
     * @param timestamp2 Second timestamp
     * @return True if same day
     */
    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        return getStartOfDay(timestamp1) == getStartOfDay(timestamp2)
    }
    
    /**
     * Check if timestamp is today.
     * @param timestamp Unix timestamp in milliseconds
     * @return True if today
     */
    fun isToday(timestamp: Long): Boolean {
        return isSameDay(timestamp, System.currentTimeMillis())
    }
    
    /**
     * Check if timestamp is yesterday.
     * @param timestamp Unix timestamp in milliseconds
     * @return True if yesterday
     */
    fun isYesterday(timestamp: Long): Boolean {
        val yesterday = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        return isSameDay(timestamp, yesterday)
    }
    
    /**
     * Get the day of week as Chinese string.
     * @param timestamp Unix timestamp in milliseconds
     * @return Day of week in Chinese (e.g., "周一", "周二")
     */
    fun getDayOfWeekChinese(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "周日"
            Calendar.MONDAY -> "周一"
            Calendar.TUESDAY -> "周二"
            Calendar.WEDNESDAY -> "周三"
            Calendar.THURSDAY -> "周四"
            Calendar.FRIDAY -> "周五"
            Calendar.SATURDAY -> "周六"
            else -> ""
        }
    }
    
    /**
     * Convert timestamp to ISO 8601 format.
     * @param timestamp Unix timestamp in milliseconds
     * @return ISO 8601 formatted string
     */
    fun toIso8601(timestamp: Long): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        format.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return format.format(Date(timestamp))
    }
    
    /**
     * Parse ISO 8601 string to timestamp.
     * @param isoString ISO 8601 formatted string
     * @return Unix timestamp in milliseconds, -1 if parsing fails
     */
    fun fromIso8601(isoString: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            format.parse(isoString)?.time ?: -1
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }
    
    /**
     * Get current timestamp.
     * @return Current Unix timestamp in milliseconds
     */
    fun now(): Long = System.currentTimeMillis()
    
    /**
     * Get timestamp offset by specified amount.
     * @param offset Offset value
     * @param unit Time unit
     * @return Timestamp offset by amount
     */
    fun offset(offset: Long, unit: TimeUnit): Long {
        return System.currentTimeMillis() + unit.toMillis(offset)
    }
}
