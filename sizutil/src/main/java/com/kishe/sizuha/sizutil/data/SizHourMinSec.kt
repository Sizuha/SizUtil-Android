package sizuha.library.slib.kotlin.datetime

import java.util.*


class SizHourMinSec() {

    var hour: Int = 0
        private set

    var minute: Int = 0
        private set

    var second: Int = 0
        private set

    var isZero: Boolean = false
        get() = hour == 0 && minute == 0 && second == 0

    constructor(hhmmss: Int): this() {
        set(hhmmss, false)
    }

    constructor(calendar: Calendar): this() {
        set(calendar, false)
    }

    constructor(hour: Int, min: Int, second: Int = 0): this() {
        set(hour, min, second, false)
    }

    fun set(hhmmss: Int, checkValidation: Boolean = true): Boolean {
        val hour = hhmmss/100_00
        val minute = (hhmmss - hour*100_00) / 100
        val second = hhmmss - (hhmmss/100)*100

        return set(hour, minute, second)
    }

    fun set(hour: Int, min: Int, second: Int = 0, checkValidation: Boolean = true): Boolean {
        return if (!checkValidation || checkValidation(hour, min, second)) {
            this.hour = hour
            this.minute = min
            this.second = second
            true
        }
        else false
    }

    fun set(calendar: Calendar, checkValidation: Boolean = true): Boolean {
        calendar.let {
            val hour = it.get(Calendar.HOUR_OF_DAY)
            val minute = it.get(Calendar.MINUTE)
            val second = it.get(Calendar.SECOND)
            return set(hour, minute, second, checkValidation)
        }
    }

    private fun checkValidation(hour: Int, min: Int, second: Int): Boolean =
            hour >= 0 && min in 0 until 60 && second in 0 until 60

    fun checkSelfValidation() = checkValidation(hour, minute, second)

    fun toInt(): Int = hour*100_00 + minute*100 + second
    fun toSeconds(): Int = hour*60*60 + minute*60 + second
    fun toMinutes(): Int = hour*60 + minute

    fun toCalendar() = Calendar.getInstance().apply {
        clear()
        set(Calendar.MILLISECOND, 0)
        set(Calendar.SECOND, second)
        set(Calendar.MINUTE, minute)
        set(Calendar.HOUR_OF_DAY, hour)
    }

    fun toDate()= toCalendar().time

    // target's seconds - self seconds
    fun diffSeconds(target: SizHourMinSec): Int = target.toSeconds() - this.toSeconds()

    companion object {

        fun from(hhmmss: Int): SizHourMinSec? = SizHourMinSec(hhmmss).let {
            if (it.checkSelfValidation()) it else null
        }

        fun fromSeconds(seconds: Int): SizHourMinSec {
            val h = seconds / 60 / 60
            val m = seconds/60 - h*60
            val s = seconds % 60

            return SizHourMinSec(h, m, s)
        }

    }

    override fun equals(other: Any?): Boolean = when (other) {
        is SizHourMinSec -> this.toInt() == other.toInt()
        else -> super.equals(other)
    }

    operator fun minus(other: SizHourMinSec): Int = other.toSeconds() - this.toSeconds()

}