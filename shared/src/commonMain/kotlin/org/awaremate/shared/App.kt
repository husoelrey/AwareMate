package org.awaremate.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import org.awaremate.shared.domain.repository.PreferencesRepository
import org.awaremate.shared.presentation.navigation.RootScreen
import org.awaremate.shared.presentation.theme.AwareMateTheme
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinContext {
        val preferencesRepository: PreferencesRepository = koinInject()
        val prefs by preferencesRepository.getPreferences().collectAsState(initial = null)

        val themeMode = prefs?.themeMode ?: "SYSTEM"
        val dynamicColor = prefs?.dynamicColorEnabled ?: true

        AwareMateTheme(
            themeMode = themeMode,
            dynamicColor = dynamicColor
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription = "AwareMate Application Root Surface"
                    }
            ) {
                Navigator(RootScreen()) { navigator ->
                    FadeTransition(navigator)
                }
            }
        }
    }
}
