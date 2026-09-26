package com.studyoffline.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studyoffline.app.data.preferences.UserPreferencesRepository
import com.studyoffline.app.domain.CountdownCalculator
import com.studyoffline.app.ui.components.*
import com.studyoffline.app.ui.theme.StudyOfflineTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamCountdownDetailScreen(
    preferencesRepository: UserPreferencesRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val settings by preferencesRepository.userSettingsFlow.collectAsState(initial = null)
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    var examNameInput by remember { mutableStateOf("") }
    var selectedGoalDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        settings?.let {
            examNameInput = it.examName
            selectedGoalDate = it.examGoalDate
        }
    }

    val daysRemaining = remember(selectedGoalDate) {
        CountdownCalculator.calculateDaysRemaining(selectedGoalDate)
    }

    val formattedDate = remember(selectedGoalDate) {
        if (selectedGoalDate != null) {
            SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(selectedGoalDate!!))
        } else {
            "No exam date set"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        StudyTopBar(
            title = "Exam Countdown",
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main countdown visual
            StudyCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 24.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StudyCircularProgress(
                        progress = if (daysRemaining != null) {
                            ((100 - daysRemaining.coerceIn(0, 100)) / 100f)
                        } else 0f,
                        size = 140.dp,
                        strokeWidth = 6.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${daysRemaining ?: 0}",
                                style = typography.displayBold,
                                color = colors.accent
                            )
                            Text(
                                text = "days left",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (examNameInput.isNotEmpty()) examNameInput else "Target Exam",
                        style = typography.heading,
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formattedDate,
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Edit Goal Form
            StudyCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 16.dp
            ) {
                Text(
                    text = "Update Exam Goal",
                    style = typography.subheading,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                StudyTextField(
                    value = examNameInput,
                    onValueChange = { examNameInput = it },
                    label = "Exam / Test Name",
                    placeholder = "e.g. Biology Midterm"
                )

                Spacer(modifier = Modifier.height(14.dp))

                StudySecondaryButton(
                    text = if (selectedGoalDate != null) "Change Date: $formattedDate" else "Pick Target Date",
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                StudyPrimaryButton(
                    text = "Save Changes",
                    onClick = {
                        coroutineScope.launch {
                            preferencesRepository.setExamGoal(examNameInput.trim(), selectedGoalDate)
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Milestones Card
            StudyCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceMuted,
                contentPadding = 16.dp
            ) {
                Text(
                    text = "Automatic Countdown Milestones",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "When milestone alerts are enabled in Settings, StudyOffline will post gentle exact reminders at 7 days, 3 days, and 1 day before your target date.",
                    style = typography.body,
                    color = colors.textPrimary
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedGoalDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val chosen = datePickerState.selectedDateMillis
                        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        if (chosen != null && chosen >= todayStart) {
                            selectedGoalDate = chosen
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
