package org.awaremate.shared.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.sizeIn
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import org.awaremate.shared.hasUsageStatsPermission
import org.awaremate.shared.openBrowserUrl
import org.awaremate.shared.openUsageAccessSettings
import org.awaremate.shared.presentation.profile.ProfileScreen
import org.awaremate.shared.presentation.onboarding.OnboardingScreen

class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<SettingsScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        var showPrivacyDialog by remember { mutableStateOf(false) }
        var showDeleteAccountDialog by remember { mutableStateOf(false) }
        var hasUsageAccess by remember { mutableStateOf(hasUsageStatsPermission()) }
        val requestNotificationPermission = rememberNotificationPermissionRequester()

        LaunchedEffect(Unit) {
            while (true) {
                val granted = hasUsageStatsPermission()
                if (granted != hasUsageAccess) {
                    hasUsageAccess = granted
                }
                delay(800)
            }
        }

        LaunchedEffect(state.accountDeletionCompleted) {
            if (state.accountDeletionCompleted) navigator.replaceAll(OnboardingScreen())
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "AwareMate Settings Screen"
                }
        ) { innerPadding ->
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            contentDescription = "Loading settings"
                        }
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // 0. Profile & Identity
                    SettingsSectionTitle("Profile & Identity")

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Personal Journey",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "View level, statistics, and customize display name",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = { navigator.push(ProfileScreen()) },
                                modifier = Modifier.semantics {
                                    contentDescription = "Navigate to user profile and journey statistics"
                                }
                            ) {
                                Text("Profile")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Appearance Section
                    SettingsSectionTitle("Appearance & Theme")

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Theme Mode",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark").forEach { (mode, label) ->
                                    val isSelected = state.preferences.themeMode.equals(mode, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { screenModel.handleIntent(SettingsIntent.SetThemeMode(mode)) },
                                        label = { Text(label) },
                                        modifier = Modifier.semantics {
                                            contentDescription = "Set theme mode to $label"
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Dynamic Color (Android 12+)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Harmonize app colors with your phone's wallpaper",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = state.preferences.dynamicColorEnabled,
                                    onCheckedChange = { screenModel.handleIntent(SettingsIntent.SetDynamicColor(it)) },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Toggle dynamic color, currently ${if (state.preferences.dynamicColorEnabled) "enabled" else "disabled"}"
                                    }
                                )
                            }

                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. Awareness & Screen Time Goals
                    SettingsSectionTitle("Daily Goals & Intentions")

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Daily Screen Time Target",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            val hours = state.preferences.dailyScreenTimeGoalMinutes / 60
                            val mins = state.preferences.dailyScreenTimeGoalMinutes % 60
                            Text(
                                text = "Target: ${hours}h ${mins}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(120, 180, 240, 300).forEach { goalMins ->
                                    val isSelected = state.preferences.dailyScreenTimeGoalMinutes == goalMins
                                    val label = "${goalMins / 60}h"
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { screenModel.handleIntent(SettingsIntent.SetDailyGoal(goalMins)) },
                                        label = { Text(label) },
                                        modifier = Modifier.semantics {
                                            contentDescription = "Set daily screen time goal to $label"
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Mindful Nudge Interval",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Alert frequency: every ${state.preferences.nudgeThresholdMinutes}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(20, 30, 45, 60).forEach { intervalMins ->
                                    val isSelected = state.preferences.nudgeThresholdMinutes == intervalMins
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { screenModel.handleIntent(SettingsIntent.SetNudgeThreshold(intervalMins)) },
                                        label = { Text("${intervalMins}m") },
                                        modifier = Modifier.semantics {
                                            contentDescription = "Set nudge interval to $intervalMins minutes"
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Android System Permissions
                    SettingsSectionTitle("Permissions")

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Usage Access",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (hasUsageAccess) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF2E7D32))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "Granted",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (hasUsageAccess) {
                                    "Usage access is active. Screen time and habits are observed on this device."
                                } else {
                                    "Allows AwareMate to observe screen time and provide mindful nudges."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            if (hasUsageAccess) {
                                OutlinedButton(
                                    onClick = { openUsageAccessSettings() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics {
                                            contentDescription = "Usage access already granted. Tap to review in Android Settings."
                                        }
                                ) {
                                    Text("Manage in Settings", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                Button(
                                    onClick = { openUsageAccessSettings() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics {
                                            contentDescription = "Open Android Settings for Usage Access"
                                        }
                                ) {
                                    Text("Grant Usage Access")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Mindful Nudge Notifications",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Receive gentle notifications during the day",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = state.preferences.notificationsEnabled,
                                    onCheckedChange = {
                                        if (it) requestNotificationPermission()
                                        screenModel.handleIntent(SettingsIntent.SetNotifications(it))
                                    },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Toggle mindful notifications, currently ${if (state.preferences.notificationsEnabled) "enabled" else "disabled"}"
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Evening mood invitation",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "One gentle invitation at most, only when today has no check-in",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = state.preferences.missedCheckInReminderEnabled,
                                    onCheckedChange = {
                                        if (it) requestNotificationPermission()
                                        screenModel.handleIntent(SettingsIntent.SetMissedCheckInReminder(it))
                                    },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Toggle evening mood invitation, currently ${if (state.preferences.missedCheckInReminderEnabled) "enabled" else "disabled"}"
                                    }
                                )
                            }

                            if (state.preferences.missedCheckInReminderEnabled) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Earliest invitation time",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(18, 19, 20).forEach { hour ->
                                        FilterChip(
                                            selected = state.preferences.missedCheckInReminderHour == hour,
                                            onClick = {
                                                screenModel.handleIntent(SettingsIntent.SetMissedCheckInTime(hour))
                                            },
                                            label = { Text("${hour}:00") },
                                            modifier = Modifier.semantics {
                                                contentDescription = "Set earliest mood invitation time to ${hour}:00"
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4. Privacy & Data
                    SettingsSectionTitle("Privacy & Data")

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Your Data Stays on Your Device",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Your companion progress, screen time metrics, and mood reflections are stored locally on your device. We never sell, advertise, or track your personal information.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { showPrivacyDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .sizeIn(minHeight = 48.dp)
                                    .semantics {
                                        contentDescription = "Read full AwareMate Privacy Policy"
                                    }
                            ) {
                                Text("Privacy Policy")
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Delete my account and data",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Permanently removes your cloud account and all AwareMate data from this device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            state.accountDeletionError?.let { message ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.semantics { contentDescription = "Account deletion error: $message" }
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    screenModel.handleIntent(SettingsIntent.ClearAccountDeletionError)
                                    showDeleteAccountDialog = true
                                },
                                enabled = !state.isDeletingAccount,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .sizeIn(minHeight = 48.dp)
                                    .semantics { contentDescription = "Delete AwareMate account and all data" }
                            ) {
                                Text(if (state.isDeletingAccount) "Deleting…" else "Delete my account and data")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 5. About & Sustainability
                    SettingsSectionTitle("About & Sustainability")

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "100% Free & Open Source",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "AwareMate has no ads, no subscriptions, and no data tracking. It is licensed under Apache 2.0 and sustained by voluntary donations.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { openBrowserUrl("https://buymeacoffee.com/awaremate") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .sizeIn(minHeight = 48.dp)
                                        .semantics {
                                            contentDescription = "Support AwareMate on Buy Me a Coffee"
                                        }
                                ) {
                                    Text("Buy Coffee ☕", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { openBrowserUrl("https://github.com/sponsors/awaremate") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .sizeIn(minHeight = 48.dp)
                                        .semantics {
                                            contentDescription = "Support AwareMate via GitHub Sponsors"
                                        }
                                ) {
                                    Text("Sponsor 💖", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 6. About AwareMate
                    SettingsSectionTitle("About")

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "AwareMate v1.0.0",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Compassionate awareness companion for youth empowerment. Licensed under Apache 2.0 open-source license.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Designed with love and anti-shame principles.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                title = {
                    Text(
                        text = "AwareMate Privacy Policy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "🔒 Local-First Storage:\nAll screen time data, companion progress, and reflections stay on your device.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🚫 Zero Ads & Tracking:\nNo ads, no data broker SDKs, no selling of youth information.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "☁️ Encrypted Cloud Sync:\nFirebase Auth & Firestore are used solely for your personal cloud backup.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🗑️ Complete Data Control:\nYou can purge local and cloud records at any time.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showPrivacyDialog = false },
                        modifier = Modifier.sizeIn(minHeight = 48.dp, minWidth = 48.dp)
                    ) {
                        Text("Close")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showPrivacyDialog = false
                            openBrowserUrl("https://github.com/husoelrey/AwareMate/blob/main/docs/PRIVACY_POLICY.md")
                        },
                        modifier = Modifier.sizeIn(minHeight = 48.dp, minWidth = 48.dp)
                    ) {
                        Text("Read Online")
                    }
                }
            )
        }

        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAccountDialog = false },
                title = { Text("Delete account and data?") },
                text = {
                    Text(
                        "This permanently removes your companion, check-ins, reflections, focus history, and cloud account. This cannot be undone."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteAccountDialog = false
                            screenModel.handleIntent(SettingsIntent.DeleteAccount)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.sizeIn(minHeight = 48.dp, minWidth = 48.dp)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteAccountDialog = false },
                        modifier = Modifier.sizeIn(minHeight = 48.dp, minWidth = 48.dp)
                    ) {
                        Text("Keep my account")
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .semantics {
                contentDescription = "Settings Section: $title"
            }
    )
}
