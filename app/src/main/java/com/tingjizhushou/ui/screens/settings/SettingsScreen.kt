package com.tingjizhushou.ui.screens.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Crown
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Globe
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tingjizhushou.BuildConfig
import com.tingjizhushou.R
import com.tingjizhushou.data.model.UserEntity
import com.tingjizhushou.ui.viewmodel.SubscriptionViewModel
import com.tingjizhushou.ui.viewmodel.UserViewModel

/**
 * Settings screen composable with language selection and subscription entry.
 * Provides app configuration options including language settings and subscription.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("tingji_settings", Context.MODE_PRIVATE) }
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    
    val isDeveloperMode by subscriptionViewModel.subscriptionStatus.collectAsState().map { it.isDeveloperMode }
    val currentUser by userViewModel.currentUser.collectAsState()
    
    var autoSaveEnabled by remember { mutableStateOf(prefs.getBoolean("auto_save", true)) }
    var darkModeEnabled by remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }
    var selectedLanguage by remember { mutableStateOf(prefs.getString("language", "zh-CN") ?: "zh-CN") }
    var showLanguageMenu by remember { mutableStateOf(false) }
    
    val languages = listOf(
        "zh-CN" to stringResource(R.string.chinese),
        "en-US" to stringResource(R.string.english)
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_settings),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // User Status Card
            UserStatusCard(
                currentUser = currentUser,
                onAdminLogin = { navController?.navigate("admin-login") },
                onLogout = { userViewModel.logout() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subscription Card
            SettingsCard(title = stringResource(R.string.subscription)) {
                SettingsItem(
                    icon = Icons.Filled.Crown,
                    title = stringResource(R.string.manage_subscription),
                    subtitle = stringResource(R.string.manage_subscription_desc),
                    onClick = { navController?.navigate("subscription") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Developer Mode Card (only visible in debug builds)
            if (BuildConfig.DEBUG) {
                SettingsCard(title = stringResource(R.string.developer_mode)) {
                    SettingsSwitchItem(
                        icon = Icons.Filled.DeveloperMode,
                        title = stringResource(R.string.enable_developer_mode),
                        subtitle = stringResource(R.string.developer_mode_desc),
                        checked = isDeveloperMode,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                subscriptionViewModel.enableDeveloperMode()
                            } else {
                                subscriptionViewModel.disableDeveloperMode()
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Language Settings Card
            SettingsCard(title = stringResource(R.string.language_settings)) {
                Box {
                    SettingsItem(
                        icon = Icons.Filled.Globe,
                        title = stringResource(R.string.default_language),
                        subtitle = languages.firstOrNull { it.first == selectedLanguage }?.second ?: "Unknown",
                        onClick = { showLanguageMenu = true }
                    )
                    
                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false }
                    ) {
                        languages.forEach { (code, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    selectedLanguage = code
                                    prefs.edit().putString("language", code).apply()
                                    showLanguageMenu = false
                                },
                                leadingIcon = {
                                    if (selectedLanguage == code) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Recording Settings Card
            SettingsCard(title = stringResource(R.string.recording_settings)) {
                SettingsSwitchItem(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = stringResource(R.string.audio_quality),
                    subtitle = stringResource(R.string.audio_quality_high),
                    checked = true,
                    onCheckedChange = { }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsSwitchItem(
                    icon = Icons.Filled.Save,
                    title = stringResource(R.string.auto_save),
                    subtitle = stringResource(R.string.auto_save_description),
                    checked = autoSaveEnabled,
                    onCheckedChange = {
                        autoSaveEnabled = it
                        prefs.edit().putBoolean("auto_save", it).apply()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Appearance Settings Card
            SettingsCard(title = stringResource(R.string.appearance)) {
                SettingsSwitchItem(
                    icon = Icons.Filled.DarkMode,
                    title = stringResource(R.string.dark_mode),
                    subtitle = stringResource(R.string.dark_mode_description),
                    checked = darkModeEnabled,
                    onCheckedChange = {
                        darkModeEnabled = it
                        prefs.edit().putBoolean("dark_mode", it).apply()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // About Card
            SettingsCard(title = stringResource(R.string.about)) {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = stringResource(R.string.version),
                    subtitle = BuildConfig.VERSION_NAME
                )
            }
        }
    }
}

@Composable
private fun UserStatusCard(
    currentUser: UserEntity?,
    onAdminLogin: () -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.user_role),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        text = currentUser?.getRoleText() ?: stringResource(R.string.not_logged_in),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                if (currentUser?.phoneNumber != null) {
                    Text(
                        text = currentUser.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (currentUser == null || !currentUser.isAdmin()) {
                    TextButton(
                        onClick = onAdminLogin,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.admin_login))
                    }
                }
                
                if (currentUser != null) {
                    TextButton(
                        onClick = onLogout,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.logout))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = { }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}