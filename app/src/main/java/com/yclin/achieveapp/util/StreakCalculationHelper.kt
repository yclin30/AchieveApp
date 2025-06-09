package com.yclin.achieveapp.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object StreakCalculationHelper {

    /**
     * 计算当前连续打卡天数和历史最长连续打卡天数
     * @param completedDates 已排序的完成日期列表 (升序)
     * @param frequencyType 1 for daily, 2 for weekly specific days
     * @param weekDays bitmask for weekly habits (e.g., Monday=1, Tuesday=2, ..., Sunday=64)
     * @param habitCreatedAt 习惯的创建日期，用于确定计算的起点
     * @return Pair<CurrentStreak, LongestStreak>
     */
    fun calculateStreaks(
        completedDates: List<LocalDate>,
        frequencyType: Int,
        weekDays: Int,
        habitCreatedAt: LocalDate
    ): Pair<Int, Int> {
        if (completedDates.isEmpty()) {
            return Pair(0, 0)
        }

        var currentStreak = 0
        var longestStreak = 0
        var potentialStreakStartDate: LocalDate? = null

        val today = LocalDate.now(java.time.ZoneOffset.UTC) // 确保与打卡日期时区一致

        // 从最后一次完成日期开始向前追溯，计算当前连续天数
        var streakOngoing = true
        var lastCheckedDate = today // 或 completedDates.last()，取决于如何定义“当前”
        var tempCurrentStreak = 0

        // 反向遍历已完成的日期，或者从今天开始向前检查
        // 这部分逻辑非常依赖于你如何定义“连续”
        // 例如，对于每日习惯：
        // 1. 如果今天完成了，从昨天开始检查，直到找到未完成的一天或习惯创建日
        // 2. 如果今天未完成，但昨天完成了，从昨天开始检查

        // 以下是一个非常简化的每日习惯的当前连续天数计算思路（需要完善）
        if (frequencyType == 1) { // 每日习惯
            var expectedDate = today
            if (completedDates.contains(today)) {
                tempCurrentStreak = 1
                expectedDate = today.minusDays(1)
            } else { // 如果今天没打卡，当前连续为0 （除非你允许昨天打卡算连续到今天）
                tempCurrentStreak = 0
                streakOngoing = false // 如果今天没打卡，当前连续就断了
            }

            while (streakOngoing && expectedDate.isAfter(habitCreatedAt.minusDays(1))) {
                if (completedDates.contains(expectedDate)) {
                    tempCurrentStreak++
                    expectedDate = expectedDate.minusDays(1)
                } else {
                    streakOngoing = false
                }
            }
            currentStreak = tempCurrentStreak
        }
        // 对于每周习惯，逻辑会更复杂，需要检查 weekDays

        // 计算历史最长连续天数 (遍历所有打卡记录)
        // 这是一个更通用的最长连续计算方法
        if (completedDates.isNotEmpty()) {
            var currentLongestCalcStreak = 0
            var previousDate: LocalDate? = null

            for (date in completedDates) {
                if (date.isBefore(habitCreatedAt)) continue // 早于创建日期的打卡不计入

                if (previousDate == null) { // 第一个有效打卡日
                    currentLongestCalcStreak = 1
                } else {
                    if (isConsecutive(previousDate, date, frequencyType, weekDays)) {
                        currentLongestCalcStreak++
                    } else {
                        // 连续中断，比较并重置
                        longestStreak = maxOf(longestStreak, currentLongestCalcStreak)
                        currentLongestCalcStreak = 1 // 新的连续从当前日期开始
                    }
                }
                previousDate = date
            }
            longestStreak = maxOf(longestStreak, currentLongestCalcStreak) // 处理最后一段连续
        }
        // 注意：上面的 currentStreak 计算还比较粗糙，需要根据你的业务逻辑精确实现
        // 尤其是如何处理“今天”以及习惯的频率

        return Pair(currentStreak, longestStreak)
    }

    /**
     * 检查两个日期是否根据习惯频率构成连续
     */
    private fun isConsecutive(
        prevDate: LocalDate,
        currDate: LocalDate,
        frequencyType: Int,
        weekDays: Int
    ): Boolean {
        if (frequencyType == 1) { // 每日
            return ChronoUnit.DAYS.between(prevDate, currDate) == 1L
        } else if (frequencyType == 2) { // 每周特定几天
            // 找到 prevDate 之后，符合 weekDays 的下一个预期打卡日
            var nextExpectedDay = prevDate.plusDays(1)
            while (nextExpectedDay.isBefore(currDate) || nextExpectedDay.isEqual(currDate)) {
                val dayBit = 1 shl (nextExpectedDay.dayOfWeek.value - 1) // Monday=0 to Sunday=6 for bitmask
                if ((weekDays and dayBit) != 0) { // 如果是预期的打卡日
                    return nextExpectedDay.isEqual(currDate) // 且这个预期的打卡日就是当前日期
                }
                if (nextExpectedDay.isEqual(currDate) && (weekDays and dayBit) == 0) {
                    // 如果到达了当前日期，但当前日期不是设定的打卡日，则不连续
                    return false
                }
                nextExpectedDay = nextExpectedDay.plusDays(1)
            }
            return false // 如果循环结束都没找到匹配的 currDate
        }
        return false
    }
}