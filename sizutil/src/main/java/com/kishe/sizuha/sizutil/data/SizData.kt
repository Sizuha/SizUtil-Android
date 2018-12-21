package com.kishe.sizuha.sizutil.data

import android.content.Context
import sizuha.library.slib.kotlin.datetime.SizYearMonthDay
import java.util.*
import java.util.concurrent.TimeUnit


fun asDip(context: Context, value: Int): Int {
    val d = context.resources.displayMetrics.density
    return (value * d).toInt()
}

fun stringListToString(source: Collection<String>): String {
    var firstItem = true
    val result = StringBuilder()

    for (item in source) {
        if (firstItem) {
            result.append(",")
            firstItem = false
        }
        else {
            result.append(item)
        }
    }
    return result.toString()
}

fun isValidEmail(target: CharSequence?): Boolean {
    return if (target == null || target.length < 3) {
        false
    }
    else {
        android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }
}

fun bytesToHex(bytes: ByteArray, lowercase: Boolean = true): String {
    val hexChars = StringBuilder()
    val format = if (lowercase) "%02X" else "%02x"

    for (b in bytes.indices) {
        hexChars.append(format.format(b))
    }
    return hexChars.toString()
}

fun parseInt(value: String, defaultValue: Int): Int {
    return parseInt(value, 10, defaultValue)
}

fun parseInt(value: String, radix: Int, defaultValue: Int): Int {
    if (value.isEmpty()) return defaultValue

    return try {
        Integer.parseInt(value, radix)
    }
    catch (e: NumberFormatException) {
        defaultValue
    }
}

fun parseFloat(value: String, defaultValue: Float): Float {
    if (value.isEmpty()) return defaultValue

    return try {
        java.lang.Float.parseFloat(value)
    }
    catch (e: NumberFormatException) {
        defaultValue
    }
}

fun parseDouble(value: String, defaultValue: Double): Double {
    if (value.isEmpty()) return defaultValue

    return try {
        java.lang.Double.parseDouble(value)
    }
    catch (e: NumberFormatException) {
        defaultValue
    }
}

//------ Date/Time

class SizCalendarDateIterator(start: Calendar, private val endInclusive: Calendar) : Iterator<Calendar> {
    private var current = start
    val isReversed = start > endInclusive

    override fun hasNext(): Boolean {
        val currDateVal = calendarToDateInt(current)
        val endDateVal = calendarToDateInt(endInclusive)

        return if (isReversed) currDateVal >= endDateVal else currDateVal <= endDateVal
    }

    override fun next(): Calendar {
        return current.apply {
            add(Calendar.DAY_OF_MONTH, if (isReversed) -1 else 1)
        }
    }

    private fun calendarToDateInt(calendar: Calendar): Int {
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH)
        val d = calendar.get(Calendar.DAY_OF_MONTH)
        return y*100_00 + m*100 + d
    }
}

class SizCalendarDateRange(override val start: Calendar, override val endInclusive: Calendar) : ClosedRange<Calendar>, Iterable<Calendar> {
    override fun iterator(): Iterator<Calendar> {
        return SizCalendarDateIterator(start, endInclusive)
    }
}

class SizDayIterator(start: SizYearMonthDay, private val endInclusive: SizYearMonthDay)
    : Iterator<SizYearMonthDay>
{

    private var current = start
    val isReversed = start > endInclusive

    override fun hasNext(): Boolean {
        return if (isReversed) current >= endInclusive else current <= endInclusive
    }

    override fun next(): SizYearMonthDay {
        return if (isReversed) current-- else current++
    }

}

class SizDayRange(override val start: SizYearMonthDay, override val endInclusive: SizYearMonthDay)
    : ClosedRange<SizYearMonthDay>, Iterable<SizYearMonthDay>
{

    val isReversed = start > endInclusive

    override fun iterator(): Iterator<SizYearMonthDay> {
        return SizDayIterator(start, endInclusive)
    }

}

fun getOnlyDate(date: Date): Date {
    val c = Calendar.getInstance()
    c.time = date

    return clearTime(c).time
}

fun clearTime(calendar: Calendar): Calendar {
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar
}

fun makeDate(year: Int, month_0base: Int, day: Int): Date {
    val c = Calendar.getInstance()
    c.set(Calendar.YEAR, year)
    c.set(Calendar.MONTH, month_0base)
    c.set(Calendar.DAY_OF_MONTH, day)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.time
}

fun toCalender(date: Date): Calendar {
    val result = Calendar.getInstance()
    result.time = date
    return result
}

fun getDaysBetween(a: Date, b: Date): Int {
    val c = Calendar.getInstance()

    c.time = a
    clearTime(c)
    val start = c.timeInMillis

    c.time = b
    clearTime(c)
    val end = c.timeInMillis

    val diff = end - start
    return TimeUnit.MILLISECONDS.toDays(diff).toInt()
}
