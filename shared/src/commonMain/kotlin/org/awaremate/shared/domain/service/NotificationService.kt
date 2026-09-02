package org.awaremate.shared.domain.service

import org.awaremate.shared.domain.model.NudgeMessage

interface NotificationService {
    fun showNudgeNotification(nudge: NudgeMessage): Result<Unit>
    fun cancelAllNotifications()
}
