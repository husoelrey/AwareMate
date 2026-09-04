package org.awaremate.shared.domain.model

data class WeeklyMoodScreenTimePoint(
    val dayLabel: String,
    val dateString: String,
    val moodScore: Int,
    val energyLevel: Int,
    val screenTimeMinutes: Int
) {
    val moodEnergyScore: Double
        get() = (moodScore + energyLevel) / 2.0
}

data class WeeklyMoodScreenTimeCorrelation(
    val hasEnoughMoodDays: Boolean,
    val points: List<WeeklyMoodScreenTimePoint> = emptyList(),
    val observationalInsight: String? = null
)
