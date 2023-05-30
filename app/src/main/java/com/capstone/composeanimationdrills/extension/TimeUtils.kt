package com.capstone.composeanimationdrills.extension

class SubtitleTimeUtils {
    companion object {
        private fun calculateTimeMs(hour: String, min: String, sec: String, ms: String): Long {
            return hour.toLong() * 3600000 + min.toLong() * 60000 + sec.toLong() * 1000 + ms.toLong()
        }

        fun parseSrtTime(srtTime: String): Pair<Long, Long>? {
            val timeRegex = Regex("""(\d{2}):(\d{2}):(\d{2}),(\d{3}) --> (\d{2}):(\d{2}):(\d{2}),(\d{3})""")
            val matchResult = timeRegex.find(srtTime)

            if (matchResult != null) {
                val (startHour, startMin, startSec, startMs, endHour, endMin, endSec, endMs) = matchResult.destructured
                val startTime = calculateTimeMs(startHour, startMin, startSec, startMs)
                val endTime = calculateTimeMs(endHour, endMin, endSec, endMs)
                return Pair(startTime, endTime)
            }

            return null
        }

        fun convertMilliseconds(milliseconds: Long): Quadruple<Long, Long, Long, Long> {
            val millisecondsPart = milliseconds % 1000
            val seconds = (milliseconds / 1000) % 60
            val minutes = (milliseconds / (1000 * 60)) % 60
            val hours = (milliseconds / (1000 * 60 * 60)) % 24
            return Quadruple(hours, minutes, seconds, millisecondsPart)
        }

        fun parseTimeToDestructedTime(time: String): Quadruple<Long, Long, Long, Long>? {
            val timeRegex = "(\\d{2}):(\\d{2}):(\\d{2}),(\\d{3})".toRegex()
            val matchResult  = timeRegex.find(time)

            return if(matchResult != null) {
                val (hour, min, sec, ms) = matchResult.destructured
                Quadruple(hour.toLong(), min.toLong(), sec.toLong(), ms.toLong())
            } else null
        }

        fun parseTimeToMillis(time: String): Long? {
            val timeRegex = "(\\d{2}):(\\d{2}):(\\d{2}),(\\d{3})".toRegex()
            val matchResult  = timeRegex.find(time)

            return if(matchResult != null) {
                val (hour, min, sec, ms) = matchResult.destructured
                calculateTimeMs(hour, min, sec, ms)
            } else null
        }

        fun getZeroSrtTime(): Pair<Long, Long> {
            return Pair(0, 0)
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
