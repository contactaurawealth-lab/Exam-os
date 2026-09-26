package com.studyoffline.app.ui.profile

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studyoffline.app.data.backup.BackupRoot
import com.studyoffline.app.data.backup.ImportValidationResult
import com.studyoffline.app.ui.components.*
import com.studyoffline.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onResetComplete: () -> Unit
) {
    val settings by viewModel.userSettings.collectAsState()
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showTimePicker by remember { mutableStateOf(false) }
    var showResetDialogStep1 by remember { mutableStateOf(false) }
    var showResetDialogStep2 by remember { mutableStateOf(false) }
    var resetInputText by remember { mutableStateOf("") }

    var importValidationSuccess by remember { mutableStateOf<BackupRoot?>(null) }
    var importWarnings by remember { mutableStateOf<List<String>>(emptyList()) }
    var showImportChoiceDialog by remember { mutableStateOf(false) }

    // SAF Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportBackup(uri) { success, errorMsg ->
                coroutineScope.launch {
                    if (success) {
                        snackbarHostState.showSnackbar("Backup exported successfully")
                    } else {
                        snackbarHostState.showSnackbar(errorMsg ?: "Failed to export backup")
                    }
                }
            }
        }
    }

    // SAF Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                when (val result = viewModel.validateImportFile(uri)) {
                    is ImportValidationResult.Success -> {
                        importValidationSuccess = result.backupRoot
                        importWarnings = result.warnings
                        showImportChoiceDialog = true
                    }
                    is ImportValidationResult.Error -> {
                        snackbarHostState.showSnackbar(result.message)
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background,
        topBar = {
            StudyTopBar(
                title = "Settings",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section 1: Theme Settings
            SettingsSection(title = "Appearance & Theme") {
                // Theme Mode Selector
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Theme Mode", style = typography.bodyStrong, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark").forEach { (mode, label) ->
                            StudyChip(
                                text = label,
                                selected = settings.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                HorizontalDivider(color = colors.divider)

                // Accent Color
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Accent Color", style = typography.bodyStrong, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PresetAccents.forEach { hex ->
                            val color = ColorContrastUtil.parseHexColor(hex)
                            val isSelected = settings.accentColorHex.equals(hex, ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { viewModel.setAccentColor(hex) }
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(2.5.dp, colors.textPrimary, CircleShape)
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    LineIcons.Check(size = 16.dp, tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Notifications
            SettingsSection(title = "Notifications & Reminders") {
                SettingsToggleRow(
                    title = "Master Notifications",
                    subtitle = "Enable or disable all app notifications",
                    checked = settings.masterNotificationEnabled,
                    onCheckedChange = { viewModel.setMasterNotification(it) }
                )

                HorizontalDivider(color = colors.divider)

                SettingsToggleRow(
                    title = "Daily Study Reminder",
                    subtitle = "Time: ${formatMinutesToTime(settings.dailyReminderTimeMinutes)}",
                    checked = settings.dailyReminderEnabled && settings.masterNotificationEnabled,
                    enabled = settings.masterNotificationEnabled,
                    onCheckedChange = { viewModel.setDailyReminder(it) },
                    onSubtitleClick = { showTimePicker = true }
                )

                HorizontalDivider(color = colors.divider)

                SettingsToggleRow(
                    title = "Flashcards Due Reminder",
                    subtitle = "Alert when spaced repetition cards are due",
                    checked = settings.reviewDueEnabled && settings.masterNotificationEnabled,
                    enabled = settings.masterNotificationEnabled,
                    onCheckedChange = { viewModel.setReviewDue(it) }
                )

                HorizontalDivider(color = colors.divider)

                SettingsToggleRow(
                    title = "Exam Milestones",
                    subtitle = "Alerts at 7, 3, and 1 day before exam",
                    checked = settings.countdownMilestonesEnabled && settings.masterNotificationEnabled,
                    enabled = settings.masterNotificationEnabled,
                    onCheckedChange = { viewModel.setCountdownMilestones(it) }
                )

                HorizontalDivider(color = colors.divider)

                SettingsToggleRow(
                    title = "Pomodoro Alerts",
                    subtitle = "Sound & vibration on session or break end",
                    checked = settings.pomodoroAlertsEnabled && settings.masterNotificationEnabled,
                    enabled = settings.masterNotificationEnabled,
                    onCheckedChange = { viewModel.setPomodoroAlerts(it) }
                )

                HorizontalDivider(color = colors.divider)

                SettingsToggleRow(
                    title = "Ongoing Session Notification",
                    subtitle = "Status bar notification during active timer",
                    checked = settings.ongoingSessionNotificationEnabled && settings.masterNotificationEnabled,
                    enabled = settings.masterNotificationEnabled,
                    onCheckedChange = { viewModel.setOngoingSessionNotification(it) }
                )

                HorizontalDivider(color = colors.divider)

                // Link to OS Notification Settings
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Manage in System Settings",
                            style = typography.body,
                            color = colors.accent
                        )
                        LineIcons.ArrowRight(size = 18.dp, tint = colors.accent)
                    }
                }
            }

            // Section 3: Widgets
            SettingsSection(title = "Home Screen Widgets") {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Glance AppWidgets",
                        style = typography.bodyStrong,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. Exam Countdown (2x1 and 4x1): Shows days remaining with a progress ring.\n2. Today's Plan (4x2): Up to 3 topic rows with tap-to-complete directly from your home screen.",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "To add: Long-press any empty space on your Android home screen, select Widgets, and choose StudyOffline.",
                        style = typography.caption,
                        color = colors.accent
                    )
                }
            }

            // Section 4: Data Management (Export / Import / Reset)
            SettingsSection(title = "Data & Backups") {
                // Export
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val filename = "studyoffline-backup-${System.currentTimeMillis()}.json"
                            exportLauncher.launch(filename)
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Export Backup (JSON)", style = typography.bodyStrong, color = colors.textPrimary)
                            Text(text = "Save all subjects, cards, and progress to file", style = typography.caption, color = colors.textSecondary)
                        }
                        LineIcons.ArrowRight(size = 18.dp, tint = colors.textSecondary)
                    }
                }

                HorizontalDivider(color = colors.divider)

                // Import
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            importLauncher.launch(arrayOf("application/json"))
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Import Backup (JSON)", style = typography.bodyStrong, color = colors.textPrimary)
                            Text(text = "Restore data with merge or replace options", style = typography.caption, color = colors.textSecondary)
                        }
                        LineIcons.ArrowRight(size = 18.dp, tint = colors.textSecondary)
                    }
                }

                HorizontalDivider(color = colors.divider)

                // Reset App (PRD §10)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showResetDialogStep1 = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Reset Application", style = typography.bodyStrong, color = colors.error)
                            Text(text = "Erase all study data and start fresh", style = typography.caption, color = colors.error.copy(alpha = 0.8f))
                        }
                        LineIcons.Trash(size = 18.dp, tint = colors.error)
                    }
                }
            }

            // Section 5: About
            SettingsSection(title = "About StudyOffline") {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "StudyOffline v1.0.0", style = typography.bodyStrong, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "100% Offline-first Architecture\nNo accounts, no telemetry, no third-party tracking. All your notes, questions, and intelligence remain strictly on your device.",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Daily Reminder Time Picker
    if (showTimePicker) {
        val currentMinutes = settings.dailyReminderTimeMinutes
        val initialHour = currentMinutes / 60
        val initialMinute = currentMinutes % 60

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                viewModel.setDailyReminder(true, hourOfDay * 60 + minute)
                showTimePicker = false
            },
            initialHour,
            initialMinute,
            false
        ).apply {
            setOnDismissListener { showTimePicker = false }
            show()
        }
    }

    // Import Choice Dialog (Merge vs Replace)
    if (showImportChoiceDialog && importValidationSuccess != null) {
        val root = importValidationSuccess!!
        AlertDialog(
            onDismissRequest = { showImportChoiceDialog = false },
            title = {
                Text("Restore Backup", style = typography.subheading, color = colors.textPrimary)
            },
            text = {
                Column {
                    Text(
                        text = "Found ${root.data.subjects.size} subjects, ${root.data.topics.size} topics, and ${root.data.flashcards.size} flashcards.",
                        style = typography.body,
                        color = colors.textPrimary
                    )
                    if (importWarnings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Warnings: ${importWarnings.size} orphaned items will be omitted.",
                            style = typography.caption,
                            color = colors.warning
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Choose how to import:\n• Merge: Append new items without modifying existing ones.\n• Replace: Completely erase existing data and restore backup.",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
            },
            confirmButton = {
                StudyPrimaryButton(
                    text = "Merge",
                    onClick = {
                        viewModel.executeImport(root, isReplace = false) { success, err ->
                            showImportChoiceDialog = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(if (success) "Backup merged successfully" else err ?: "Import failed")
                            }
                        }
                    }
                )
            },
            dismissButton = {
                StudyDestructiveButton(
                    text = "Replace",
                    onClick = {
                        viewModel.executeImport(root, isReplace = true) { success, err ->
                            showImportChoiceDialog = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(if (success) "Backup restored (replaced) successfully" else err ?: "Import failed")
                            }
                        }
                    }
                )
            },
            containerColor = colors.surface
        )
    }

    // Reset Confirmation Step 1 (PRD §10)
    if (showResetDialogStep1) {
        AlertDialog(
            onDismissRequest = { showResetDialogStep1 = false },
            title = {
                Text("Reset StudyOffline?", style = typography.subheading, color = colors.error)
            },
            text = {
                Text(
                    text = "This will delete all subjects, notes, flashcards, and progress. This cannot be undone.",
                    style = typography.body,
                    color = colors.textPrimary
                )
            },
            confirmButton = {
                StudyDestructiveButton(
                    text = "Reset Anyway",
                    onClick = {
                        showResetDialogStep1 = false
                        resetInputText = ""
                        showResetDialogStep2 = true
                    }
                )
            },
            dismissButton = {
                StudySecondaryButton(
                    text = "Export Backup First",
                    onClick = {
                        showResetDialogStep1 = false
                        exportLauncher.launch("studyoffline-backup-${System.currentTimeMillis()}.json")
                    }
                )
            },
            containerColor = colors.surface
        )
    }

    // Reset Confirmation Step 2 (Deliberate Typed confirmation: type RESET)
    if (showResetDialogStep2) {
        AlertDialog(
            onDismissRequest = { showResetDialogStep2 = false },
            title = {
                Text("Confirm Permanent Erase", style = typography.subheading, color = colors.error)
            },
            text = {
                Column {
                    Text(
                        text = "To confirm data destruction, please type RESET below:",
                        style = typography.body,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    StudyTextField(
                        value = resetInputText,
                        onValueChange = { resetInputText = it },
                        placeholder = "Type RESET"
                    )
                }
            },
            confirmButton = {
                StudyDestructiveButton(
                    text = "Confirm & Wipe Everything",
                    enabled = resetInputText == "RESET",
                    onClick = {
                        showResetDialogStep2 = false
                        viewModel.resetApp {
                            onResetComplete()
                        }
                    }
                )
            },
            dismissButton = {
                StudyTextButton(
                    text = "Cancel",
                    onClick = { showResetDialogStep2 = false },
                    color = colors.textSecondary
                )
            },
            containerColor = colors.surface
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = typography.subheading,
            color = colors.textPrimary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = colors.surfaceMuted,
            border = BorderStroke(1.dp, colors.divider)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    onSubtitleClick: (() -> Unit)? = null
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .then(if (onSubtitleClick != null) Modifier.clickable(onClick = onSubtitleClick) else Modifier)
        ) {
            Text(
                text = title,
                style = typography.bodyStrong,
                color = if (enabled) colors.textPrimary else colors.textDisabled
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = typography.caption,
                    color = if (enabled) colors.textSecondary else colors.textDisabled
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onAccent,
                checkedTrackColor = colors.accent,
                uncheckedThumbColor = colors.textDisabled,
                uncheckedTrackColor = colors.surface
            )
        )
    }
}

private fun formatMinutesToTime(minutesOfDay: Int): String {
    val h = minutesOfDay / 60
    val m = minutesOfDay % 60
    val ampm = if (h >= 12) "PM" else "AM"
    val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
    return String.format("%d:%02d %s", displayH, m, ampm)
}
