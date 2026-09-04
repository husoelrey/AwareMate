package org.awaremate.shared.domain.service

interface MissedCheckInReminderScheduler {
    suspend fun refresh()
}
