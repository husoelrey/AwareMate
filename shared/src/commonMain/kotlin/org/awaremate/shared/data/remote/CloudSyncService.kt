package org.awaremate.shared.data.remote

import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.DailyChallenge
import org.awaremate.shared.domain.model.FocusSession
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.model.User

interface CloudSyncService {
    suspend fun backupUser(user: User): Result<Unit>
    suspend fun backupCompanion(companion: Companion): Result<Unit>
    suspend fun backupMoodEntry(entry: MoodEntry): Result<Unit>
    suspend fun backupFocusSession(session: FocusSession): Result<Unit>
    suspend fun backupDailyChallenge(challenge: DailyChallenge): Result<Unit>
    suspend fun fetchCloudCompanion(userId: String): Result<Companion?>
    suspend fun fetchCloudMoodEntries(userId: String): Result<List<MoodEntry>>
}
