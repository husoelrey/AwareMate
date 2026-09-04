package org.awaremate.shared.presentation.settings

import androidx.compose.runtime.Composable

@Composable
expect fun rememberNotificationPermissionRequester(): () -> Unit
