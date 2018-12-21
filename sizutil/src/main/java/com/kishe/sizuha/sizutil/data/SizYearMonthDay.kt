package sizuha.library.slib.kotlin.datetime

import com.kishe.sizuha.sizutil.data.SizDayRange
import java.util.*

class SizYearMonthDay : Comparable<SizYearMonthDay> {

    var year = 0
        private set

    var month = 0
        private set

    var day = 0
        private set

    constructor(yyyymmdd: Int) {
        set(yyyymmdd, false)
    }

    constructor(year: Int, month: Int, day: Int) {
        set(year, month, day, false)
    }

    constructor(calendar: Calendar) {
        set(calendar, false)
    }

    fun set(yyyymmdd: Int, checkValidation: Boolean = true): Boolean {
        val year = yyyymmdd / 100_00
        val month = (yyyymmdd - year*100_00) / 100
        val day = yyyymmdd - (yyyymmdd/100)*100

        return set(year, month, day, checkValidation)
    }

    fun set(year: Int, month: Int, day: Int, checkValidation: Boolean = true): Boolean {
        return if (!checkValidation || checkValidation(year, month, day)) {
            this.year = year
            this.month = month
            this.day = day
            true
        }
        else false
    }

    fun set(calendar: Calendar, checkValidation: Boolean = true): Boolean {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return set(year, month, day, checkValidation)
    }

    fun checkSelfValidation() = checkValidation(year, month, day)

    private fun checkValidation(year: Int, month: Int, day: Int): Boolean {
        val firstFilter = when {
            year < 0 -> false
            month !in 1..12 -> false
            month == 2 && day !in 1..29 -> false
            month in listOf(4,6,9,11) && day !in 1..30 -> false
            day !in 1..31 -> false
            else -> true
        }

        return if (firstFilter) {
            Calendar.getInstance().run {
                set(year, month-1, 1)

                val maxDayOfMonth = getActualMaximum(Calendar.DAY_OF_MONTH)
                day <= maxDayOfMonth
            }
        }
        else false
    }

    fun toInt(): Int = year*100_00 + month*100 + day

    fun toCalendar() = Calendar.getInstance().apply {
        clear()
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month-1)
        set(Calendar.DAY_OF_MONTH, day)
    }

    fun toDate(): Date = toCalendar().time

    companion object {

        fun from(yyyymmdd: Int): SizYearMonthDay? =
                SizYearMonthDay(yyyymmdd).let {
                    if (it.checkSelfValidation()) it else null
                }

    }

    override fun equals(other: Any?): Boolean = when (other) {
        is SizYearMonthDay -> this.toInt() == other.toInt()
        else -> super.equals(other)
    }

    /**
     * 日付A(self)が日付B(from)から何日(days)過ぎているか
     *
     * A(this) - B(from) = (total milliseconds of A) - (total milliseconds of B) / 1000.0 / 60.0 / 60.0 / 24.0
     *
     * @return days
     */
    fun diffDays(from: SizYearMonthDay): Int {
        val a = this.toCalendar().timeInMillis
        val b = from.toCalendar().timeInMillis
        return ((a-b) / 1000.0 / 60.0 / 60.0 / 24.0).toInt()
    }

    fun diffMonth(from: SizYearMonthDay): Int {
        val monthCountSelf = year*12 + month
        val monthCountFrom = from.year*12 + from.month

        val result = monthCountSelf - monthCountFrom -
                (if (monthCountSelf != monthCountFrom && from.day > day) 1 else 0)

        return if (result < 0) 0 else result
    }

    operator fun minus(other: SizYearMonthDay): Int = diffDays(other)

    override operator fun compareTo(other: SizYearMonthDay): Int = this.toInt() - other.toInt()

    operator fun inc(): SizYearMonthDay {
        val cal = toCalendar().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }

        return SizYearMonthDay(cal)
    }

    operator fun dec(): SizYearMonthDay {
        val cal = toCalendar().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }

        return SizYearMonthDay(cal)
    }

    operator fun rangeTo(that: SizYearMonthDay): SizDayRange {
        return SizDayRange(this, that)
    }

}