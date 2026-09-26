package com.studyoffline.app.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyoffline.app.domain.CountdownCalculator
import com.studyoffline.app.ui.components.*
import com.studyoffline.app.ui.theme.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            onComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyOfflineTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header with Back button and Step indicator
            if (state.step > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { viewModel.previousStep() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        LineIcons.ArrowLeft(size = 24.dp)
                    }

                    Text(
                        text = "Step ${state.step} of 6",
                        style = StudyOfflineTheme.typography.caption,
                        color = StudyOfflineTheme.colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (state.step) {
                    0 -> WelcomeStep(
                        onGetStarted = { viewModel.nextStep() },
                        onSkip = { viewModel.skipSetup() }
                    )
                    1 -> StudyGoalStep(
                        examName = state.examName,
                        targetDateMillis = state.examGoalDate,
                        onGoalChange = { name, date -> viewModel.setExamGoal(name, date) },
                        onNext = { viewModel.nextStep() }
                    )
                    2 -> SubjectSelectionStep(
                        subjects = state.availableSubjects,
                        customInput = state.customSubjectInput,
                        errorMessage = state.subjectError,
                        onToggle = { viewModel.toggleSubject(it) },
                        onCustomInputChange = { viewModel.setCustomSubjectInput(it) },
                        onAddCustom = { viewModel.addCustomSubject() },
                        onNext = { viewModel.nextStep() }
                    )
                    3 -> DailyStudyTimeStep(
                        selectedMinutes = state.dailyStudyMinutes,
                        customMinutes = state.customMinutesInput,
                        reminderTimeMinutes = state.reminderTimeMinutes,
                        onMinutesChange = { viewModel.setDailyStudyMinutes(it) },
                        onCustomMinutesChange = { viewModel.setCustomMinutes(it) },
                        onReminderTimeChange = { viewModel.setReminderTime(it) },
                        onNext = { viewModel.nextStep() }
                    )
                    4 -> NotificationPermissionStep(
                        onNext = { viewModel.nextStep() }
                    )
                    5 -> ThemePreviewStep(
                        selectedMode = state.themeMode,
                        selectedAccent = state.accentHex,
                        onThemeChange = { mode, accent -> viewModel.setTheme(mode, accent) },
                        onNext = { viewModel.nextStep() }
                    )
                    6 -> ReadyStep(
                        state = state,
                        onStart = { viewModel.finishOnboarding() }
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(
    onGetStarted: () -> Unit,
    onSkip: () -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo mark
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(colors.surfaceMuted, RoundedCornerShape(20.dp))
                .border(1.dp, colors.divider, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            LineIcons.Book(size = 36.dp, tint = colors.accent)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "StudyOffline",
            style = typography.displayBold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "A calm, distraction-free study companion.\nOffline-first, private, and focused on mastery.",
            style = typography.body,
            color = colors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        StudyPrimaryButton(
            text = "Get Started",
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        StudyTextButton(
            text = "Skip setup",
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StudyGoalStep(
    examName: String,
    targetDateMillis: Long?,
    onGoalChange: (String, Long?) -> Unit,
    onNext: () -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    var currentName by remember { mutableStateOf(examName) }
    var selectedDate by remember { mutableStateOf(targetDateMillis) }
    var showDatePicker by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf<String?>(null) }

    val formattedDate = remember(selectedDate) {
        if (selectedDate != null) {
            SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(selectedDate!!))
        } else {
            "No target date (optional)"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "What are you preparing for?",
            style = typography.heading,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Set an exam name and target date to unlock the countdown tracker.",
            style = typography.body,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(28.dp))

        StudyTextField(
            value = currentName,
            onValueChange = {
                currentName = it
                onGoalChange(currentName, selectedDate)
            },
            label = "Exam / Goal Name (optional)",
            placeholder = "e.g., MCAT, Final Exams, Bar Exam"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Target Date (optional)",
            style = typography.caption,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))

        StudyCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showDatePicker = true },
            contentPadding = 14.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LineIcons.Calendar(size = 20.dp, tint = colors.accent)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = formattedDate,
                    style = typography.body,
                    color = if (selectedDate != null) colors.textPrimary else colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                if (selectedDate != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable {
                                selectedDate = null
                                onGoalChange(currentName, null)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        LineIcons.Close(size = 16.dp, tint = colors.textSecondary)
                    }
                }
            }
        }

        if (dateError != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateError.orEmpty(),
                style = typography.caption,
                color = colors.error
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        StudyPrimaryButton(
            text = "Continue",
            onClick = {
                onGoalChange(currentName, selectedDate)
                onNext()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val chosen = datePickerState.selectedDateMillis
                        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        if (chosen != null && chosen < todayStart) {
                            dateError = "Target date must be today or later"
                        } else {
                            dateError = null
                            selectedDate = chosen
                            onGoalChange(currentName, chosen)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubjectSelectionStep(
    subjects: List<OnboardingSubjectItem>,
    customInput: String,
    errorMessage: String?,
    onToggle: (String) -> Unit,
    onCustomInputChange: (String) -> Unit,
    onAddCustom: () -> Unit,
    onNext: () -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Choose your subjects",
            style = typography.heading,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select what you plan to study. Each subject receives an accessible pastel tag.",
            style = typography.body,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Multi-select chip grid
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            subjects.forEach { item ->
                StudyChip(
                    text = item.name,
                    selected = item.isSelected,
                    colorDotHex = item.colorHex,
                    onClick = { onToggle(item.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Custom subject input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudyTextField(
                value = customInput,
                onValueChange = onCustomInputChange,
                placeholder = "Add custom subject...",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StudySecondaryButton(
                text = "Add",
                onClick = onAddCustom,
                enabled = customInput.trim().isNotEmpty()
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage,
                style = typography.caption,
                color = colors.error
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        StudyPrimaryButton(
            text = "Continue",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DailyStudyTimeStep(
    selectedMinutes: Int,
    customMinutes: String,
    reminderTimeMinutes: Int,
    onMinutesChange: (Int) -> Unit,
    onCustomMinutesChange: (String) -> Unit,
    onReminderTimeChange: (Int) -> Unit,
    onNext: () -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    val presets = listOf(30, 60, 90)

    val reminderTimeFormatted = remember(reminderTimeMinutes) {
        val h = reminderTimeMinutes / 60
        val m = reminderTimeMinutes % 60
        val ampm = if (h >= 12) "PM" else "AM"
        val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
        String.format("%d:%02d %s", displayH, m, ampm)
    }

    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Daily study target",
            style = typography.heading,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "How much time do you want to spend studying each day?",
            style = typography.body,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Preset segments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { min ->
                val isSelected = selectedMinutes == min && customMinutes.isEmpty()
                val bg = if (isSelected) colors.surfaceSelected else colors.surfaceMuted
                val border = if (isSelected) BorderStroke(1.dp, colors.accent) else null
                val textTint = if (isSelected) colors.accent else colors.textPrimary

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onCustomMinutesChange("")
                            onMinutesChange(min)
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = bg,
                    border = border
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$min min",
                            style = typography.bodyStrong,
                            color = textTint
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        StudyTextField(
            value = customMinutes,
            onValueChange = onCustomMinutesChange,
            placeholder = "Custom minutes (e.g. 45)",
            label = "Or specify custom minutes"
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Daily reminder time",
            style = typography.caption,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))

        StudyCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showTimePicker = true },
            contentPadding = 14.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LineIcons.Timer(size = 20.dp, tint = colors.accent)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = reminderTimeFormatted,
                    style = typography.bodyStrong,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Change",
                    style = typography.caption,
                    color = colors.accent
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        StudyPrimaryButton(
            text = "Continue",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showTimePicker) {
        val initialHour = reminderTimeMinutes / 60
        val initialMinute = reminderTimeMinutes % 60
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onReminderTimeChange(timePickerState.hour * 60 + timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text("OK", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@Composable
private fun NotificationPermissionStep(
    onNext: () -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Continue whether granted or denied
        onNext()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        StudyCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 24.dp
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colors.surfaceSelected, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                LineIcons.Timer(size = 24.dp, tint = colors.accent)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Gentle, offline reminders",
                style = typography.subheading,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "StudyOffline reminds you when it's time to review — no ads, no spam, and entirely managed on-device without internet access.",
                style = typography.body,
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        StudyPrimaryButton(
            text = "Enable Notifications",
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    onNext()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        StudyTextButton(
            text = "Maybe later",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ThemePreviewStep(
    selectedMode: String,
    selectedAccent: String,
    onThemeChange: (String, String) -> Unit,
    onNext: () -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    var customHexInput by remember { mutableStateOf("") }
    var contrastWarning by remember { mutableStateOf<String?>(null) }

    fun verifyContrast(hex: String) {
        val color = ColorContrastUtil.parseHexColor(hex)
        val meets = ColorContrastUtil.meetsWcagAa(color, colors.background)
        contrastWarning = if (!meets) "This color may be hard to read on buttons" else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Appearance",
            style = typography.heading,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Personalize the visual tone. Contrast is checked against WCAG AA standards.",
            style = typography.body,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Mode: System / Light / Dark
        Text(text = "Theme mode", style = typography.caption, color = colors.textSecondary)
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark").forEach { (mode, label) ->
                val isSelected = selectedMode == mode
                StudyChip(
                    text = label,
                    selected = isSelected,
                    onClick = { onThemeChange(mode, selectedAccent) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Accent presets
        Text(text = "Accent color", style = typography.caption, color = colors.textSecondary)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PresetAccents.forEach { hex ->
                val color = ColorContrastUtil.parseHexColor(hex)
                val isSelected = selectedAccent.equals(hex, ignoreCase = true)

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable {
                            verifyContrast(hex)
                            onThemeChange(selectedMode, hex)
                        }
                        .then(
                            if (isSelected) {
                                Modifier.border(2.5.dp, colors.textPrimary, CircleShape)
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        LineIcons.Check(size = 18.dp, tint = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Hex input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudyTextField(
                value = customHexInput,
                onValueChange = {
                    customHexInput = it
                    if (it.length == 7 && it.startsWith("#")) {
                        verifyContrast(it)
                        onThemeChange(selectedMode, it)
                    }
                },
                placeholder = "Custom hex e.g. #7C9A82",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StudySecondaryButton(
                text = "Apply",
                onClick = {
                    val formatted = if (customHexInput.startsWith("#")) customHexInput else "#$customHexInput"
                    verifyContrast(formatted)
                    onThemeChange(selectedMode, formatted)
                },
                enabled = customHexInput.isNotEmpty()
            )
        }

        if (contrastWarning != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = contrastWarning.orEmpty(),
                style = typography.caption,
                color = colors.warning
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Live preview card
        Text(text = "Live preview", style = typography.caption, color = colors.textSecondary)
        Spacer(modifier = Modifier.height(6.dp))

        StudyCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(ColorContrastUtil.parseHexColor(selectedAccent), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kinematics & Dynamics",
                    style = typography.subheading,
                    color = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "3 flashcards due today · Scheduled for revision",
                style = typography.caption,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            StudyLinearProgress(progress = 0.65f)
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        StudyPrimaryButton(
            text = "Continue",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ReadyStep(
    state: OnboardingState,
    onStart: () -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    val selectedSubjects = state.availableSubjects.filter { it.isSelected }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "You're all set",
            style = typography.displayBold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Here is a summary of your study setup.",
            style = typography.body,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        StudyCard(modifier = Modifier.fillMaxWidth()) {
            if (state.examName.isNotEmpty()) {
                Text(
                    text = state.examName,
                    style = typography.subheading,
                    color = colors.textPrimary
                )
                if (state.examGoalDate != null) {
                    val days = CountdownCalculator.calculateDaysRemaining(state.examGoalDate)
                    Text(
                        text = "$days days remaining",
                        style = typography.caption,
                        color = colors.accent
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.divider)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = "Subjects (${selectedSubjects.size}):",
                style = typography.caption,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                selectedSubjects.forEach { subj ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(ColorContrastUtil.parseHexColor(subj.colorHex), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = subj.name,
                            style = typography.body,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = colors.divider)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Daily study target",
                    style = typography.body,
                    color = colors.textSecondary
                )
                Text(
                    text = "${state.dailyStudyMinutes} min / day",
                    style = typography.bodyStrong,
                    color = colors.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        StudyPrimaryButton(
            text = "Start Studying",
            onClick = onStart,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
