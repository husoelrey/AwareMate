package org.awaremate.shared.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.CompanionStage
import org.awaremate.shared.domain.model.DailyChallenge
import org.awaremate.shared.domain.model.FocusSession
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.model.User

private const val SYNC_TIMEOUT_MS = 2500L

class AndroidFirestoreSyncService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CloudSyncService {

    override suspend fun backupUser(user: User): Result<Unit> = runCatching {
        withTimeout(SYNC_TIMEOUT_MS) {
            val data = mapOf(
                "id" to user.id,
                "displayName" to user.displayName,
                "email" to user.email,
                "isAnonymous" to user.isAnonymous,
                "createdAtEpochMs" to user.createdAtEpochMs,
                "lastActiveEpochMs" to user.lastActiveEpochMs
            )
            firestore.collection("users").document(user.id).set(data, SetOptions.merge()).await()
        }
    }

    override suspend fun backupCompanion(companion: Companion): Result<Unit> = runCatching {
        withTimeout(SYNC_TIMEOUT_MS) {
            val data = mapOf(
                "id" to companion.id,
                "name" to companion.name,
                "stage" to companion.stage.name,
                "emotion" to companion.emotion.name,
                "experiencePoints" to companion.experiencePoints,
                "momentumScore" to companion.momentumScore,
                "happinessXp" to companion.happinessXp,
                "energyXp" to companion.energyXp,
                "wisdomXp" to companion.wisdomXp,
                "creativityXp" to companion.creativityXp,
                "lastUpdatedEpochMs" to companion.lastUpdatedEpochMs
            )
            firestore.collection("companions").document(companion.id).set(data, SetOptions.merge()).await()
        }
    }

    override suspend fun backupMoodEntry(entry: MoodEntry): Result<Unit> = runCatching {
        withTimeout(SYNC_TIMEOUT_MS) {
            val data = mapOf(
                "id" to entry.id,
                "userId" to entry.userId,
                "timestampEpochMs" to entry.timestampEpochMs,
                "emoji" to entry.emoji,
                "moodScore" to entry.moodScore,
                "energyLevel" to entry.energyLevel,
                "note" to entry.note,
                "tags" to entry.tags
            )
            firestore.collection("mood_entries").document(entry.id).set(data, SetOptions.merge()).await()
        }
    }

    override suspend fun backupFocusSession(session: FocusSession): Result<Unit> = runCatching {
        withTimeout(SYNC_TIMEOUT_MS) {
            val data = mapOf(
                "id" to session.id,
                "userId" to session.userId,
                "startTimeEpochMs" to session.startTimeEpochMs,
                "durationSeconds" to session.durationSeconds,
                "category" to session.category.name,
                "earnedXp" to session.earnedXp,
                "completed" to session.completed,
                "note" to session.note
            )
            firestore.collection("focus_sessions").document(session.id).set(data, SetOptions.merge()).await()
        }
    }

    override suspend fun backupDailyChallenge(challenge: DailyChallenge): Result<Unit> = runCatching {
        withTimeout(SYNC_TIMEOUT_MS) {
            val data = mapOf(
                "id" to challenge.id,
                "userId" to challenge.userId,
                "title" to challenge.title,
                "description" to challenge.description,
                "category" to challenge.category.name,
                "xpReward" to challenge.xpReward,
                "dateString" to challenge.dateString,
                "completed" to challenge.completed,
                "completedAtEpochMs" to challenge.completedAtEpochMs
            )
            firestore.collection("daily_challenges").document(challenge.id).set(data, SetOptions.merge()).await()
        }
    }

    override suspend fun fetchCloudCompanion(userId: String): Result<Companion?> = runCatching {
        withTimeout(SYNC_TIMEOUT_MS) {
            val snapshot = firestore.collection("companions").document(userId).get().await()
            if (!snapshot.exists()) return@withTimeout null
            Companion(
                id = snapshot.getString("id") ?: userId,
                name = snapshot.getString("name") ?: "Sprout",
                stage = runCatching { CompanionStage.valueOf(snapshot.getString("stage") ?: "") }.getOrDefault(CompanionStage.SEED),
                emotion = runCatching { CompanionEmotion.valueOf(snapshot.getString("emotion") ?: "") }.getOrDefault(CompanionEmotion.PEACEFUL),
                experiencePoints = snapshot.getLong("experiencePoints")?.toInt() ?: 0,
                momentumScore = snapshot.getDouble("momentumScore") ?: 1.0,
                happinessXp = snapshot.getLong("happinessXp")?.toInt() ?: 0,
                energyXp = snapshot.getLong("energyXp")?.toInt() ?: 0,
                wisdomXp = snapshot.getLong("wisdomXp")?.toInt() ?: 0,
                creativityXp = snapshot.getLong("creativityXp")?.toInt() ?: 0,
                lastUpdatedEpochMs = snapshot.getLong("lastUpdatedEpochMs") ?: 0L
            )
        }
    }

    override suspend fun fetchCloudMoodEntries(userId: String): Result<List<MoodEntry>> = runCatching {
        withTimeout(SYNC_TIMEOUT_MS) {
            val snapshot = firestore.collection("mood_entries")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val timestamp = doc.getLong("timestampEpochMs") ?: return@mapNotNull null
                val emoji = doc.getString("emoji") ?: "🌱"
                val score = doc.getLong("moodScore")?.toInt() ?: 3
                val energy = doc.getLong("energyLevel")?.toInt() ?: 3
                val note = doc.getString("note")
                @Suppress("UNCHECKED_CAST")
                val tags = doc.get("tags") as? List<String> ?: emptyList()
                MoodEntry(
                    id = id,
                    userId = userId,
                    timestampEpochMs = timestamp,
                    emoji = emoji,
                    moodScore = score,
                    energyLevel = energy,
                    note = note,
                    tags = tags,
                    isSynced = true
                )
            }
        }
    }
}
