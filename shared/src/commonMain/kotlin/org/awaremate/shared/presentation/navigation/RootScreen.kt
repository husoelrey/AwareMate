package org.awaremate.shared.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.awaremate.shared.domain.repository.PreferencesRepository
import org.awaremate.shared.presentation.main.MainScreen
import org.awaremate.shared.presentation.onboarding.OnboardingScreen
import org.koin.compose.koinInject

class RootScreen : Screen {

    @Composable
    override fun Content() {
        val preferencesRepository: PreferencesRepository = koinInject()
        val prefsFlow = preferencesRepository.getPreferences()
        val prefs by prefsFlow.collectAsState(initial = null)
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(prefs) {
            val userPrefs = prefs ?: return@LaunchedEffect
            if (userPrefs.onboardingCompleted) {
                navigator.replaceAll(MainScreen())
            } else {
                navigator.replaceAll(OnboardingScreen())
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .semantics {
                    contentDescription = "AwareMate Launch Splash Screen"
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🌱", fontSize = 52.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "AwareMate",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Compassionate Growth Companion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                CircularProgressIndicator(
                    modifier = Modifier
                        .size(32.dp)
                        .semantics {
                            contentDescription = "Loading AwareMate"
                        },
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
