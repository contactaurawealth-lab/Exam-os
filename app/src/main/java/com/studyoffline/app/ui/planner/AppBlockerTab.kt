package com.studyoffline.app.ui.planner

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyoffline.app.ui.blocker.AppBlockerViewModel
import com.studyoffline.app.ui.components.*
import com.studyoffline.app.ui.theme.StudyOfflineTheme

@Composable
fun AppBlockerTab(
    viewModel: AppBlockerViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography
    val context = LocalContext.current

    var showPasscodeDialog by remember { mutableStateOf(false) }

    // Re-check accessibility permission when resuming
    DisposableEffect(Unit) {
        viewModel.checkAccessibilityPermission()
        onDispose { }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Master Blocker Switch Card
        item {
            StudyCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "App Blocker",
                            style = typography.subheading,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Block distracting apps to maintain deep focus",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }

                    Switch(
                        checked = state.isBlockerEnabled,
                        onCheckedChange = { viewModel.toggleBlockerEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.surface,
                            checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.textDisabled,
                            uncheckedTrackColor = colors.surfaceMuted
                        )
                    )
                }
            }
        }

        // Accessibility Permission Notice (if disabled)
        if (!state.isAccessibilityEnabled) {
            item {
                StudyCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surfaceMuted
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            LineIcons.Settings(size = 20.dp, tint = colors.accent)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Permission Required",
                                style = typography.bodyStrong,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Enable Accessibility to detect when blocked apps are opened.",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    StudyPrimaryButton(
                        text = "Enable in Accessibility Settings",
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Passcode Setup & 5-Min Grace Period Card
        item {
            StudyCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Emergency Unlock Passcode",
                            style = typography.bodyStrong,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (state.passcode.isNotBlank()) {
                                "Passcode: " + "•".repeat(state.passcode.length)
                            } else {
                                "No passcode set (unlock directly)"
                            },
                            style = typography.caption,
                            color = if (state.passcode.isNotBlank()) colors.accent else colors.textSecondary
                        )
                    }

                    StudySecondaryButton(
                        text = if (state.passcode.isBlank()) "Set Passcode" else "Change",
                        onClick = { showPasscodeDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Entering this passcode unlocks the blocked app for a 5-minute emergency grace period before re-locking.",
                    style = typography.caption,
                    color = colors.textSecondary
                )
            }
        }

        // App List Header & Search
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Installed Apps",
                        style = typography.subheading,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${state.blockedCount} blocked",
                        style = typography.caption,
                        color = if (state.blockedCount > 0) colors.accent else colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                StudyTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = "Search installed apps...",
                    singleLine = true,
                    trailingIcon = {
                        LineIcons.Search(size = 18.dp, tint = colors.textSecondary)
                    }
                )
            }
        }

        // App Items
        if (state.isLoadingApps) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.accent)
                }
            }
        } else if (state.filteredApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No apps found matching '${state.searchQuery}'",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
            }
        } else {
            items(state.filteredApps, key = { it.packageName }) { app ->
                StudyCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 12.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Letter badge icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (app.isBlocked) colors.accent.copy(alpha = 0.15f) else colors.surfaceMuted),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = app.appName.firstOrNull()?.uppercase() ?: "?",
                                style = typography.bodyStrong,
                                color = if (app.isBlocked) colors.accent else colors.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.appName,
                                style = typography.bodyStrong,
                                color = colors.textPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = app.packageName,
                                style = typography.caption.copy(fontSize = 11.sp),
                                color = colors.textSecondary,
                                maxLines = 1
                            )
                        }

                        Switch(
                            checked = app.isBlocked,
                            onCheckedChange = { viewModel.toggleAppBlocked(app.packageName) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.surface,
                                checkedTrackColor = colors.accent,
                                uncheckedThumbColor = colors.textDisabled,
                                uncheckedTrackColor = colors.surfaceMuted
                            )
                        )
                    }
                }
            }
        }
    }

    // Set / Change Passcode Dialog
    if (showPasscodeDialog) {
        var inputPasscode by remember { mutableStateOf(state.passcode) }

        AlertDialog(
            onDismissRequest = { showPasscodeDialog = false },
            title = {
                Text(
                    text = "Set Unlock Passcode",
                    style = typography.heading,
                    color = colors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a password or PIN required to unlock blocked apps for 5 minutes.",
                        style = typography.body,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    StudyTextField(
                        value = inputPasscode,
                        onValueChange = { inputPasscode = it },
                        placeholder = "e.g. 1234 or password",
                        label = "Passcode",
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                StudyPrimaryButton(
                    text = "Save",
                    onClick = {
                        viewModel.setPasscode(inputPasscode)
                        showPasscodeDialog = false
                    }
                )
            },
            dismissButton = {
                StudyTextButton(
                    text = "Cancel",
                    onClick = { showPasscodeDialog = false }
                )
            },
            containerColor = colors.surface
        )
    }
}
