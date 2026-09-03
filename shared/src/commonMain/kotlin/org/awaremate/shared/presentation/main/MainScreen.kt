package org.awaremate.shared.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
            NavTabItem(title = "Home", selectedIcon = Icons.Filled.Home, unselectedIcon = Icons.Outlined.Home, contentDescription = "Home navigation tab"),
            NavTabItem(title = "Companion", selectedIcon = Icons.Filled.Spa, unselectedIcon = Icons.Outlined.Spa, contentDescription = "Companion avatar and growth tab"),
            NavTabItem(title = "Focus", selectedIcon = Icons.Filled.Timer, unselectedIcon = Icons.Outlined.Timer, contentDescription = "Mindful focus timer tab"),
            NavTabItem(title = "Growth", selectedIcon = Icons.Filled.AutoAwesome, unselectedIcon = Icons.Outlined.AutoAwesome, contentDescription = "Personal growth and mood reflection tab"),
            NavTabItem(title = "Settings", selectedIcon = Icons.Filled.Settings, unselectedIcon = Icons.Outlined.Settings, contentDescription = "Settings and preferences tab")
        )

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.semantics {
                        contentDescription = "Main bottom navigation bar"
                    }
                ) {
                    tabs.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.contentDescription,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String
)
