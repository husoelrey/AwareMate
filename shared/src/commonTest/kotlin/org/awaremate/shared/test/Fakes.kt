package org.awaremate.shared.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.awaremate.shared.data.local.dao.CompanionDao
import org.awaremate.shared.data.local.dao.DailyChallengeDao
import org.awaremate.shared.data.local.dao.FocusSessionDao
import org.awaremate.shared.data.local.dao.MoodEntryDao
import org.awaremate.shared.data.local.dao.UserDao
import org.awaremate.shared.data.local.entity.CompanionEntity
import org.awaremate.shared.data.local.entity.DailyChallengeEntity
import org.awaremate.shared.data.local.entity.FocusSessionEntity
import org.awaremate.shared.data.local.entity.MoodEntryEntity
import org.awaremate.shared.data.local.entity.UserEntity
import org.awaremate.shared.data.remote.AuthService
import org.awaremate.shared.data.remote.CloudSyncService
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.DailyChallenge
import org.awaremate.shared.domain.model.FocusSession
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.model.User

class FakeUserDao : UserDao {
    private val users = MutableStateFlow<Map<String, UserEntity>>(emptyMap())

    override fun getCurrentUserFlow(): Flow<UserEntity?> = users.map { it.values.firstOrNull() }

    override suspend fun getUserById(id: String): UserEntity? = users.value[id]

    override suspend fun insertUser(user: UserEntity) {
        users.value = users.value + (user.id to user)
    }

    override suspend fun deleteUser(id: String) {
        users.value = users.value - id
    }

    override suspend fun clearAllUsers() {
        users.value = emptyMap()
    }
}

class FakeCompanionDao : CompanionDao {
    private val companions = MutableStateFlow<Map<String, CompanionEntity>>(emptyMap())

    override fun getCompanionFlow(id: String): Flow<CompanionEntity?> = companions.map { it[id] }

    override suspend fun getCompanionById(id: String): CompanionEntity? = companions.value[id]

    override suspend fun insertCompanion(companion: CompanionEntity) {
        companions.value = companions.value + (companion.id to companion)
    }

    override suspend fun clearAllCompanions() {
        companions.value = emptyMap()
    }
}

class FakeMoodEntryDao : MoodEntryDao {
    private val entries = MutableStateFlow<List<MoodEntryEntity>>(emptyList())

    override fun getAllMoodEntriesFlow(): Flow<List<MoodEntryEntity>> = entries

    override fun getMoodEntriesForRange(startEpochMs: Long, endEpochMs: Long): Flow<List<MoodEntryEntity>> =
        entries.map { list ->
            list.filter { it.timestampEpochMs in startEpochMs..endEpochMs }
        }

    override suspend fun getUnsyncedEntries(): List<MoodEntryEntity> =
        entries.value.filter { !it.isSynced }

    override suspend fun insertMoodEntry(entry: MoodEntryEntity) {
        entries.value = entries.value.filterNot { it.id == entry.id } + entry
    }

    override suspend fun markAsSynced(id: String, synced: Boolean) {
        entries.value = entries.value.map {
            if (it.id == id) it.copy(isSynced = synced) else it
        }
    }

    override suspend fun deleteMoodEntry(id: String) {
        entries.value = entries.value.filterNot { it.id == id }
    }

    override suspend fun clearAllMoodEntries() {
        entries.value = emptyList()
    }
}

class FakeDailyChallengeDao : DailyChallengeDao {
    private val challenges = MutableStateFlow<List<DailyChallengeEntity>>(emptyList())

    override fun getChallengesForDateFlow(dateString: String): Flow<List<DailyChallengeEntity>> =
        challenges.map { list -> list.filter { it.dateString == dateString } }

    override suspend fun getUnsyncedChallenges(): List<DailyChallengeEntity> =
        challenges.value.filter { !it.isSynced }

    override suspend fun insertChallenges(challenges: List<DailyChallengeEntity>) {
        val existingMap = this.challenges.value.associateBy { it.id }.toMutableMap()
        challenges.forEach { existingMap[it.id] = it }
        this.challenges.value = existingMap.values.toList()
    }

    override suspend fun insertChallenge(challenge: DailyChallengeEntity) {
        insertChallenges(listOf(challenge))
    }

    override suspend fun markAsCompleted(id: String, completedAt: Long) {
        challenges.value = challenges.value.map {
            if (it.id == id) it.copy(completed = true, completedAtEpochMs = completedAt, isSynced = false) else it
        }
    }

    override suspend fun markAsSynced(id: String, synced: Boolean) {
        challenges.value = challenges.value.map {
            if (it.id == id) it.copy(isSynced = synced) else it
        }
    }

    override suspend fun clearAllChallenges() {
        challenges.value = emptyList()
    }
}

class FakeFocusSessionDao : FocusSessionDao {
    private val sessions = MutableStateFlow<List<FocusSessionEntity>>(emptyList())

    override fun getRecentSessionsFlow(limit: Int): Flow<List<FocusSessionEntity>> =
        sessions.map { it.take(limit) }

    override suspend fun getUnsyncedSessions(): List<FocusSessionEntity> =
        sessions.value.filter { !it.isSynced }

    override suspend fun getTotalFocusSeconds(): Long? =
        sessions.value.filter { it.completed }.sumOf { it.durationSeconds.toLong() }

    override suspend fun insertSession(session: FocusSessionEntity) {
        sessions.value = listOf(session) + sessions.value.filterNot { it.id == session.id }
    }

    override suspend fun markAsSynced(id: String, synced: Boolean) {
        sessions.value = sessions.value.map {
            if (it.id == id) it.copy(isSynced = synced) else it
        }
    }

    override suspend fun clearAllSessions() {
        sessions.value = emptyList()
    }
}

class FakeCloudSyncService : CloudSyncService {
    val backedUpUsers = mutableListOf<User>()
    val backedUpCompanions = mutableListOf<Companion>()
    val backedUpMoods = mutableListOf<MoodEntry>()
    val backedUpSessions = mutableListOf<FocusSession>()
    val backedUpChallenges = mutableListOf<DailyChallenge>()

    override suspend fun backupUser(user: User): Result<Unit> {
        backedUpUsers.add(user)
        return Result.success(Unit)
    }

    override suspend fun backupCompanion(companion: Companion): Result<Unit> {
        backedUpCompanions.add(companion)
        return Result.success(Unit)
    }

    override suspend fun backupMoodEntry(entry: MoodEntry): Result<Unit> {
        backedUpMoods.add(entry)
        return Result.success(Unit)
    }

    override suspend fun backupFocusSession(session: FocusSession): Result<Unit> {
        backedUpSessions.add(session)
        return Result.success(Unit)
    }

    override suspend fun backupDailyChallenge(challenge: DailyChallenge): Result<Unit> {
        backedUpChallenges.add(challenge)
        return Result.success(Unit)
    }

    override suspend fun fetchCloudCompanion(userId: String): Result<Companion?> =
        Result.success(backedUpCompanions.lastOrNull())

    override suspend fun fetchCloudMoodEntries(userId: String): Result<List<MoodEntry>> =
        Result.success(backedUpMoods)
}

class FakeAuthService : AuthService {
    private val _authState = MutableStateFlow<User?>(null)
    override val authState: Flow<User?> = _authState
    override val currentUser: User? get() = _authState.value

    override suspend fun signInAnonymously(): Result<User> {
        val user = User(id = "anon-123", displayName = "Explorer", isAnonymous = true)
        _authState.value = user
        return Result.success(user)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        val user = User(id = "google-123", displayName = "Google User", email = "test@awaremate.org", isAnonymous = false)
        _authState.value = user
        return Result.success(user)
    }

    override suspend fun signOut(): Result<Unit> {
        _authState.value = null
        return Result.success(Unit)
    }
}

class FakePreferencesRepository(
    initialPreferences: org.awaremate.shared.domain.model.UserPreferences = org.awaremate.shared.domain.model.UserPreferences()
) : org.awaremate.shared.domain.repository.PreferencesRepository {
    private val _prefs = MutableStateFlow(initialPreferences)

    override fun getPreferences(): Flow<org.awaremate.shared.domain.model.UserPreferences> = _prefs

    override suspend fun updatePreferences(
        transform: (org.awaremate.shared.domain.model.UserPreferences) -> org.awaremate.shared.domain.model.UserPreferences
    ): Result<Unit> {
        _prefs.value = transform(_prefs.value)
        return Result.success(Unit)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit> {
        _prefs.value = _prefs.value.copy(onboardingCompleted = completed)
        return Result.success(Unit)
    }
}

class FakeScreenTimeDao : org.awaremate.shared.data.local.dao.ScreenTimeDao {
    private val snapshots = MutableStateFlow<Map<String, org.awaremate.shared.data.local.entity.ScreenTimeSnapshotEntity>>(emptyMap())

    override fun getSnapshotFlow(dateString: String): Flow<org.awaremate.shared.data.local.entity.ScreenTimeSnapshotEntity?> =
        snapshots.map { it[dateString] }

    override suspend fun getSnapshot(dateString: String): org.awaremate.shared.data.local.entity.ScreenTimeSnapshotEntity? =
        snapshots.value[dateString]

    override suspend fun getSnapshots(dateStrings: List<String>): List<org.awaremate.shared.data.local.entity.ScreenTimeSnapshotEntity> =
        snapshots.value.filterKeys { it in dateStrings }.values.toList()

    override suspend fun insertSnapshot(snapshot: org.awaremate.shared.data.local.entity.ScreenTimeSnapshotEntity) {
        snapshots.value = snapshots.value + (snapshot.dateString to snapshot)
    }

    override suspend fun deleteSnapshot(dateString: String) {
        snapshots.value = snapshots.value - dateString
    }
}

class FakeHobbyDao : org.awaremate.shared.data.local.dao.HobbyDao {
    private val hobbies = MutableStateFlow<Map<String, org.awaremate.shared.data.local.entity.HobbyEntity>>(emptyMap())

    override fun getAllHobbiesFlow(): Flow<List<org.awaremate.shared.data.local.entity.HobbyEntity>> =
        hobbies.map { it.values.toList() }

    override fun getBookmarkedHobbiesFlow(): Flow<List<org.awaremate.shared.data.local.entity.HobbyEntity>> =
        hobbies.map { it.values.filter { h -> h.isBookmarked } }

    override fun getHobbiesByCategoryFlow(category: String): Flow<List<org.awaremate.shared.data.local.entity.HobbyEntity>> =
        hobbies.map { it.values.filter { h -> h.category == category } }

    override suspend fun getHobbyById(id: String): org.awaremate.shared.data.local.entity.HobbyEntity? =
        hobbies.value[id]

    override suspend fun insertDefaultHobbies(hobbies: List<org.awaremate.shared.data.local.entity.HobbyEntity>) {
        val current = this.hobbies.value.toMutableMap()
        hobbies.forEach { current.putIfAbsent(it.id, it) }
        this.hobbies.value = current
    }

    override suspend fun setBookmark(id: String, bookmarked: Boolean) {
        val current = hobbies.value[id] ?: return
        hobbies.value = hobbies.value + (id to current.copy(isBookmarked = bookmarked))
    }

    override suspend fun incrementSessionCount(id: String, timestamp: Long) {
        val current = hobbies.value[id] ?: return
        hobbies.value = hobbies.value + (id to current.copy(
            sessionsCompleted = current.sessionsCompleted + 1,
            lastCompletedEpochMs = timestamp
        ))
    }

    override suspend fun getHobbyCount(): Int = hobbies.value.size
}

class FakeSelfDiscoveryPromptDao : org.awaremate.shared.data.local.dao.SelfDiscoveryPromptDao {
    private val prompts = MutableStateFlow<Map<String, org.awaremate.shared.data.local.entity.SelfDiscoveryPromptEntity>>(emptyMap())

    override fun getAllPromptsFlow(): Flow<List<org.awaremate.shared.data.local.entity.SelfDiscoveryPromptEntity>> =
        prompts.map { it.values.toList() }

    override suspend fun getPromptById(id: String): org.awaremate.shared.data.local.entity.SelfDiscoveryPromptEntity? =
        prompts.value[id]

    override suspend fun insertDefaultPrompts(prompts: List<org.awaremate.shared.data.local.entity.SelfDiscoveryPromptEntity>) {
        val current = this.prompts.value.toMutableMap()
        prompts.forEach { current.putIfAbsent(it.id, it) }
        this.prompts.value = current
    }

    override suspend fun savePromptReflection(id: String, reflection: String?, timestamp: Long) {
        val current = prompts.value[id] ?: return
        prompts.value = prompts.value + (id to current.copy(
            isAcknowledged = true,
            userReflection = reflection,
            lastAnsweredEpochMs = timestamp
        ))
    }

    override suspend fun getPromptCount(): Int = prompts.value.size
}

