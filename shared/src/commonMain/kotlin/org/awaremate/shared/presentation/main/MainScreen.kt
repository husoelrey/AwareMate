package org.awaremate.shared.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import org.awaremate.shared.presentation.companion.CompanionScreen
import org.awaremate.shared.presentation.focus.FocusScreen
import org.awaremate.shared.presentation.growth.GrowthScreen
import org.awaremate.shared.presentation.home.HomeScreen
import org.awaremate.shared.presentation.settings.SettingsScreen

class MainScreen(
    private val initialTab: Int = 0
) : Screen {

    @Composable
    override fun Content() {
        var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }

        val tabs = listOf(
            NavTabItem(title = "Home", icon = "🏠", contentDescription = "Home navigation tab"),
            NavTabItem(title = "Companion", icon = "🌱", contentDescription = "Companion avatar and growth tab"),
            NavTabItem(title = "Focus", icon = "🎯", contentDescription = "Mindful focus timer tab"),
            NavTabItem(title = "Growth", icon = "✨", contentDescription = "Personal growth and mood reflection tab"),
            NavTabItem(title = "Settings", icon = "⚙️", contentDescription = "Settings and preferences tab")
        )

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.semantics {
                        contentDescription = "Main bottom navigation bar"
                    }
                ) {
                    tabs.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            icon = { Text(text = item.icon, fontSize = 20.sp) },
                            label = { Text(text = item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.semantics {
                                contentDescription = item.contentDescription
                            }
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "AwareMate Main Container"
                }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(onNavigateToTab = { selectedTab = it }).Content()
                    1 -> CompanionScreen().Content()
                    2 -> FocusScreen().Content()
                    3 -> GrowthScreen().Content()
                    4 -> SettingsScreen().Content()
                }
            }
        }
    }
}

private data class NavTabItem(
    val title: String,
    val icon: String,
    val contentDescription: String
)
