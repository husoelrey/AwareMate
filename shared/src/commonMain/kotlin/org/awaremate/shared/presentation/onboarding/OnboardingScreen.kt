package org.awaremate.shared.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import org.awaremate.shared.hasUsageStatsPermission
import org.awaremate.shared.openUsageAccessSettings
import org.awaremate.shared.presentation.main.MainScreen
import org.awaremate.shared.presentation.settings.rememberNotificationPermissionRequester

class OnboardingScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<OnboardingScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(state.isCompleted) {
            if (state.isCompleted) {
                navigator.replaceAll(MainScreen())
            }
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "Onboarding flow screen"
                }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Step Progress Indicator
                    val stepFraction = state.currentStep.stepNumber.toFloat() / state.currentStep.totalSteps.toFloat()
                    LinearProgressIndicator(
                        progress = { stepFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .semantics {
                                contentDescription = "Onboarding progress step ${state.currentStep.stepNumber} of ${state.currentStep.totalSteps}"
                            }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedContent(
                        targetState = state.currentStep,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        modifier = Modifier.weight(1f)
                    ) { step ->
                        when (step) {
                            OnboardingStep.WELCOME -> WelcomeStep(
                                onNext = { screenModel.handleIntent(OnboardingIntent.NextStep) }
                            )

                            OnboardingStep.WHY_IT_EXISTS -> WhyItExistsStep(
                                onBack = { screenModel.handleIntent(OnboardingIntent.PreviousStep) },
                                onNext = { screenModel.handleIntent(OnboardingIntent.NextStep) }
                            )

                            OnboardingStep.INTERESTS -> InterestsStep(
                                selectedInterests = state.selectedInterests,
                                onToggleInterest = { screenModel.handleIntent(OnboardingIntent.ToggleInterest(it)) },
                                onBack = { screenModel.handleIntent(OnboardingIntent.PreviousStep) },
                                onNext = { screenModel.handleIntent(OnboardingIntent.NextStep) }
                            )

                            OnboardingStep.COMPANION_NAMING -> CompanionNamingStep(
                                companionName = state.companionName,
                                onNameChange = { screenModel.handleIntent(OnboardingIntent.SetCompanionName(it)) },
                                onBack = { screenModel.handleIntent(OnboardingIntent.PreviousStep) },
                                onNext = { screenModel.handleIntent(OnboardingIntent.NextStep) }
                            )

                            OnboardingStep.PERMISSIONS -> PermissionsStep(
                                notificationsEnabled = state.notificationsEnabled,
                                onToggleNotifications = { screenModel.handleIntent(OnboardingIntent.SetNotificationsEnabled(it)) },
                                onOpenUsageSettings = { openUsageAccessSettings() },
                                onBack = { screenModel.handleIntent(OnboardingIntent.PreviousStep) },
                                onNext = { screenModel.handleIntent(OnboardingIntent.NextStep) }
                            )

                            OnboardingStep.INTENTIONS -> IntentionsStep(
                                dailyGoalMinutes = state.dailyScreenTimeGoalMinutes,
                                nudgeMinutes = state.nudgeThresholdMinutes,
                                bedtimeHour = state.bedtimeHour,
                                bedtimeMinute = state.bedtimeMinute,
                                onDailyGoalChange = { screenModel.handleIntent(OnboardingIntent.SetScreenTimeGoal(it)) },
                                onNudgeChange = { screenModel.handleIntent(OnboardingIntent.SetNudgeThreshold(it)) },
                                onBedtimeChange = { h, m -> screenModel.handleIntent(OnboardingIntent.SetBedtime(h, m)) },
                                onBack = { screenModel.handleIntent(OnboardingIntent.PreviousStep) },
                                onFinish = { screenModel.handleIntent(OnboardingIntent.FinishOnboarding) },
                                isLoading = state.isLoading,
                                errorMessage = state.errorMessage
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WhyItExistsStep(onBack: () -> Unit, onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .semantics { contentDescription = "Why AwareMate exists onboarding step" },
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "🪞", fontSize = 54.sp)
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "A mirror, not a game to win",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "AwareMate is a low-pressure companion for noticing your own digital and personal habits over time.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("There are no perfect streaks and no score you have to beat.")
                    Text("Your check-ins and screen-time patterns stay focused on your own experience—not comparisons with anyone else.")
                    Text("AwareMate can support reflection and gentle habit changes, but it is not therapy, medical care, or a tool for controlling you.")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Go back to welcome" }
            ) {
                Text("Back")
            }
            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Continue after reading why AwareMate exists" }
            ) {
                Text("Continue →")
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌱", fontSize = 56.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Meet AwareMate",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your compassionate companion for digital balance & mindful living.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Anti-Shame Philosophy Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💚 A Friend, Not a Warden",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No shame streaks. No rigid locks. Just gentle awareness, mindful reflections, and a companion who grows with you.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .semantics {
                    contentDescription = "Begin My Journey button"
                }
        ) {
            Text(text = "Begin My Journey", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestsStep(
    selectedInterests: Set<UserInterest>,
    onToggleInterest: (UserInterest) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "What would you like to nurture?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select one or more habits you'd like to explore gently.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            UserInterest.entries.forEach { interest ->
                val isSelected = selectedInterests.contains(interest)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onToggleInterest(interest) }
                        .semantics {
                            contentDescription = "Toggle interest ${interest.title}, currently ${if (isSelected) "selected" else "unselected"}"
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = interest.icon, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = interest.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = interest.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (isSelected) "✓" else "+",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Go back to welcome step" }
            ) {
                Text(text = "Back")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Continue to companion naming" }
            ) {
                Text(text = "Continue")
            }
        }
    }
}

@Composable
private fun CompanionNamingStep(
    companionName: String,
    onNameChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val suggestions = listOf("Sprout", "Sage", "Fern", "Flora", "Leafy", "Pip")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Your Growth Companion",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Every grand tree starts as a tiny seed. Give your companion a warm name:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Seed preview visual
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌰", fontSize = 52.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = companionName,
                onValueChange = onNameChange,
                label = { Text("Companion Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Companion name input field"
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Suggested names:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.take(3).forEach { suggestion ->
                    FilterChip(
                        selected = companionName == suggestion,
                        onClick = { onNameChange(suggestion) },
                        label = { Text(suggestion) },
                        modifier = Modifier.semantics {
                            contentDescription = "Select suggested name $suggestion"
                        }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.drop(3).forEach { suggestion ->
                    FilterChip(
                        selected = companionName == suggestion,
                        onClick = { onNameChange(suggestion) },
                        label = { Text(suggestion) },
                        modifier = Modifier.semantics {
                            contentDescription = "Select suggested name $suggestion"
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Go back to interests step" }
            ) {
                Text(text = "Back")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onNext,
                enabled = companionName.isNotBlank(),
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Continue to permissions step" }
            ) {
                Text(text = "Continue")
            }
        }
    }
}

@Composable
private fun PermissionsStep(
    notificationsEnabled: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    onOpenUsageSettings: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var hasUsageAccess by remember { mutableStateOf(hasUsageStatsPermission()) }
    val requestNotificationPermission = rememberNotificationPermissionRequester()

    // Live polling when returning from Android Settings
    LaunchedEffect(Unit) {
        while (true) {
            val granted = hasUsageStatsPermission()
            if (granted != hasUsageAccess) {
                hasUsageAccess = granted
            }
            delay(600)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Permissions & Awareness",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "AwareMate needs a couple of permissions to observe your digital habits and gently support you.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Usage Access Guidance Card (Explicitly detailed per requirements)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (hasUsageAccess) {
                        Color(0xFFE8F5E9).copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "App Usage Access",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (hasUsageAccess) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF2E7D32))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
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

                    Spacer(modifier = Modifier.height(8.dp))

                    if (hasUsageAccess) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFC8E6C9).copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Usage access enabled",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1B5E20)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "AwareMate can now observe your daily screen time and provide gentle focus nudges.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "To observe daily screen time and deliver gentle nudges, Android requires Usage Access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Your usage habits remain on your device and are never sold or shared.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Step by step instruction banner
                        Text(
                            text = "How to enable:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Tap 'Open Settings' below.\n2. Find 'AwareMate' in the list.\n3. Turn 'Permit usage access' on, then return here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (hasUsageAccess) {
                        OutlinedButton(
                            onClick = onOpenUsageSettings,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "Usage access granted. Tap to review in Android Settings"
                                }
                        ) {
                            Text(
                                text = "Manage in Settings",
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Button(
                            onClick = onOpenUsageSettings,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "Open Android Settings for Usage Access"
                                }
                        ) {
                            Text(text = "Open Usage Settings")
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "(Optional for now — you can always grant this later in Settings)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔔", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mindful Nudges",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Receive gentle nudges for focus sessions and digital sunset reminders.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            if (it) requestNotificationPermission()
                            onToggleNotifications(it)
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Toggle mindful notifications switch, currently ${if (notificationsEnabled) "enabled" else "disabled"}"
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Go back to companion naming" }
            ) {
                Text(text = "Back")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    if (notificationsEnabled) requestNotificationPermission()
                    onNext()
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Continue to daily intentions" }
            ) {
                Text(text = if (hasUsageAccess) "Continue →" else "Continue")
            }
        }
    }
}

@Composable
private fun IntentionsStep(
    dailyGoalMinutes: Int,
    nudgeMinutes: Int,
    bedtimeHour: Int,
    bedtimeMinute: Int,
    onDailyGoalChange: (Int) -> Unit,
    onNudgeChange: (Int) -> Unit,
    onBedtimeChange: (Int, Int) -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    isLoading: Boolean,
    errorMessage: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Set Your Daily Intentions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Define gentle guideposts for your day. You can change these anytime in Settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Screen time target card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎯 Daily Screen Time Target",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${dailyGoalMinutes / 60}h ${dailyGoalMinutes % 60}m per day",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = dailyGoalMinutes.toFloat(),
                        onValueChange = { onDailyGoalChange(it.toInt()) },
                        valueRange = 60f..360f,
                        steps = 9,
                        modifier = Modifier.semantics {
                            contentDescription = "Daily screen time target slider, current value ${dailyGoalMinutes / 60} hours ${dailyGoalMinutes % 60} minutes"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Nudge interval card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⏱️ Mindful Nudge Frequency",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gentle alert every $nudgeMinutes minutes of continuous use",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(20, 30, 45, 60).forEach { mins ->
                            FilterChip(
                                selected = nudgeMinutes == mins,
                                onClick = { onNudgeChange(mins) },
                                label = { Text("${mins}m") },
                                modifier = Modifier.semantics {
                                    contentDescription = "Set nudge frequency to $mins minutes"
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bedtime digital sunset card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🌙 Digital Sunset / Bedtime",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val formattedTime = "${bedtimeHour.toString().padStart(2, '0')}:${bedtimeMinute.toString().padStart(2, '0')}"
                    Text(
                        text = "Wind down time: $formattedTime",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(Pair(21, 30), Pair(22, 0), Pair(22, 30), Pair(23, 0)).forEach { (h, m) ->
                            val isSelected = bedtimeHour == h && bedtimeMinute == m
                            val label = "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
                            FilterChip(
                                selected = isSelected,
                                onClick = { onBedtimeChange(h, m) },
                                label = { Text(label) },
                                modifier = Modifier.semantics {
                                    contentDescription = "Set digital sunset bedtime to $label"
                                }
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "⚠️ $errorMessage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onBack,
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Go back to permissions step" }
            ) {
                Text(text = "Back")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onFinish,
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1.5f)
                    .semantics { contentDescription = "Complete onboarding and enter AwareMate" }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = "Plant My Seed & Begin")
                }
            }
        }
    }
}
