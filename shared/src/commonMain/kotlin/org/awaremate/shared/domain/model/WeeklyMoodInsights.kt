package org.awaremate.shared.domain.model

data class DailyMoodPoint(
    val dayLabel: String,
    val moodScore: Int?,
    val emoji: String?
)

data class WeeklyMoodInsights(
    val totalCheckIns: Int,
    val averageMoodScore: Double,
    val averageEnergyLevel: Double,
    val dominantEmoji: String,
    val dominantMoodLabel: String,
    val dailyMoodPoints: List<DailyMoodPoint>,
    val compassionateInsight: String,
    val completedChallengesThisWeek: Int = 0,
    val mindfulBreathingMinutes: Int = 0
)
