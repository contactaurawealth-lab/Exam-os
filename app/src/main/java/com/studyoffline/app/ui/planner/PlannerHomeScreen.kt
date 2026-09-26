package com.studyoffline.app.ui.planner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyoffline.app.data.model.Topic
import com.studyoffline.app.service.PomodoroForegroundService
import com.studyoffline.app.ui.components.*
import com.studyoffline.app.ui.theme.StudyOfflineTheme
import java.time.format.DateTimeFormatter

enum class PlannerSubTab {
    CALENDAR,
    POMODORO
}

@Composable
fun PlannerHomeScreen(
    viewModel: PlannerViewModel,
    onNavigateToTopic: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography
    val context = LocalContext.current

    var currentSubTab by remember { mutableStateOf(PlannerSubTab.CALENDAR) }
    var showAssignTopicSheet by remember { mutableStateOf(false) }

    // Keep screen on effect
    DisposableEffect(state.keepScreenOn, state.isPomodoroRunning) {
        val window = (context as? Activity)?.window
        if (state.keepScreenOn && state.isPomodoroRunning) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Sync Foreground Service with Pomodoro running state
    LaunchedEffect(state.isPomodoroRunning, state.pomodoroSecondsRemaining) {
        val serviceIntent = Intent(context, PomodoroForegroundService::class.java).apply {
            action = if (state.isPomodoroRunning) {
                PomodoroForegroundService.ACTION_START
            } else {
                PomodoroForegroundService.ACTION_PAUSE
            }
            putExtra(PomodoroForegroundService.EXTRA_SECONDS_REMAINING, state.pomodoroSecondsRemaining)
            putExtra(PomodoroForegroundService.EXTRA_MODE_NAME, state.pomodoroMode.title)
        }
        if (state.isPomodoroRunning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        StudyTopBar(title = "Planner")

        // Sub-tab selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StudyChip(
                text = "Revision Calendar",
                selected = currentSubTab == PlannerSubTab.CALENDAR,
                onClick = { currentSubTab = PlannerSubTab.CALENDAR },
                modifier = Modifier.weight(1f)
            )

            StudyChip(
                text = "Pomodoro Timer",
                selected = currentSubTab == PlannerSubTab.POMODORO,
                onClick = { currentSubTab = PlannerSubTab.POMODORO },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (currentSubTab) {
                PlannerSubTab.CALENDAR -> {
                    CalendarView(
                        state = state,
                        onSelectDate = { viewModel.selectDate(it) },
                        onPrevWeek = { viewModel.previousWeek() },
                        onNextWeek = { viewModel.nextWeek() },
                        onOpenAssignSheet = { showAssignTopicSheet = true },
                        onUnassignTopic = { viewModel.unassignTopic(it) },
                        onToggleCompletion = { id, comp -> viewModel.toggleTopicCompletion(id, comp) },
                        onNavigateToTopic = onNavigateToTopic
                    )
                }
                PlannerSubTab.POMODORO -> {
                    PomodoroView(
                        state = state,
                        onSelectMode = { viewModel.setPomodoroMode(it) },
                        onToggleRunning = { viewModel.togglePomodoro() },
                        onReset = {
                            val stopIntent = Intent(context, PomodoroForegroundService::class.java).apply {
                                action = PomodoroForegroundService.ACTION_STOP
                            }
                            context.startService(stopIntent)
                            viewModel.resetPomodoro()
                        },
                        onToggleKeepScreenOn = { viewModel.toggleKeepScreenOn() }
                    )
                }
            }
        }
    }

    if (showAssignTopicSheet) {
        AssignTopicBottomSheet(
            incompleteTopics = state.incompleteTopics,
            selectedDate = state.selectedDate,
            onDismiss = { showAssignTopicSheet = false },
            onAssign = { topicId ->
                viewModel.assignTopicToDate(topicId, state.selectedDate)
                showAssignTopicSheet = false
            }
        )
    }
}

@Composable
private fun CalendarView(
    state: PlannerUiState,
    onSelectDate: (java.time.LocalDate) -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onOpenAssignSheet: () -> Unit,
    onUnassignTopic: (Long) -> Unit,
    onToggleCompletion: (Long, Boolean) -> Unit,
    onNavigateToTopic: (Long) -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    val monthYearText = state.selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

    Column(modifier = Modifier.fillMaxSize()) {
        // Week Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthYearText,
                style = typography.subheading,
                color = colors.textPrimary
            )

            Row {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onPrevWeek() },
                    contentAlignment = Alignment.Center
                ) {
                    LineIcons.ArrowLeft(size = 18.dp, tint = colors.textPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onNextWeek() },
                    contentAlignment = Alignment.Center
                ) {
                    LineIcons.ArrowRight(size = 18.dp, tint = colors.textPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Week Days Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            state.weekDays.forEach { item ->
                val isSelected = item.isSelected
                val bg = if (isSelected) colors.surfaceSelected else colors.surface
                val border = if (isSelected) colors.accent else colors.divider

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, border, RoundedCornerShape(12.dp))
                        .background(bg)
                        .clickable { onSelectDate(item.date) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.dayOfWeek,
                        style = typography.label,
                        color = if (isSelected) colors.accent else colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${item.dayOfMonth}",
                        style = typography.bodyStrong.copy(
                            fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) colors.accent else colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Load indicator dot (Design Spec §5.5)
                    val loadColor = when (item.load) {
                        DayLoad.NONE -> Color.Transparent
                        DayLoad.LIGHT -> colors.accent
                        DayLoad.MEDIUM -> colors.warning
                        DayLoad.HEAVY -> colors.error
                    }

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(loadColor, CircleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Scheduled Topics for Selected Day
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dateFormatted = state.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
            Text(
                text = dateFormatted,
                style = typography.subheading,
                color = colors.textPrimary
            )

            StudyTextButton(
                text = "+ Assign Topic",
                onClick = onOpenAssignSheet
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.scheduledTopics.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                StudyEmptyState(
                    title = "No topics scheduled",
                    description = "Keep your study load balanced by scheduling topics for revision.",
                    actionButtonText = "Assign Topic",
                    onActionClick = onOpenAssignSheet
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(state.scheduledTopics, key = { it.id }) { topic ->
                    StudyCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigateToTopic(topic.id) },
                        contentPadding = 14.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(
                                        1.5.dp,
                                        if (topic.isCompleted) colors.success else colors.divider,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .background(
                                        if (topic.isCompleted) colors.success.copy(alpha = 0.15f) else Color.Transparent
                                    )
                                    .clickable { onToggleCompletion(topic.id, topic.isCompleted) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (topic.isCompleted) {
                                    LineIcons.Check(size = 14.dp, tint = colors.success)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = topic.name,
                                style = typography.body.copy(
                                    color = if (topic.isCompleted) colors.textSecondary else colors.textPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            // Unassign action
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { onUnassignTopic(topic.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                LineIcons.Close(size = 14.dp, tint = colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PomodoroView(
    state: PlannerUiState,
    onSelectMode: (PomodoroMode) -> Unit,
    onToggleRunning: () -> Unit,
    onReset: () -> Unit,
    onToggleKeepScreenOn: () -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    val minutes = state.pomodoroSecondsRemaining / 60
    val seconds = state.pomodoroSecondsRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val progress = if (state.pomodoroTotalSeconds > 0) {
        state.pomodoroSecondsRemaining.toFloat() / state.pomodoroTotalSeconds
    } else 0f

    // Warning color when < 20% remaining (Design Spec §6.6)
    val ringColor = if (progress < 0.20f) colors.warning else colors.accent

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Mode Selector: Focus / Short Break / Long Break
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PomodoroMode.values().forEach { mode ->
                val isSelected = state.pomodoroMode == mode
                StudyChip(
                    text = mode.title,
                    selected = isSelected,
                    onClick = { onSelectMode(mode) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Large Circular Timer (Design Spec §6.6 & §8.6)
        StudyCircularProgress(
            progress = progress,
            size = 220.dp,
            strokeWidth = 6.dp,
            progressColor = ringColor
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeFormatted,
                    style = typography.displayBold.copy(fontSize = 44.sp, lineHeight = 48.sp),
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.pomodoroMode.title.uppercase(),
                    style = typography.label,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Ongoing session status row (Design Spec §8.6)
        if (state.isPomodoroRunning) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceSelected)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                LineIcons.Timer(size = 16.dp, tint = colors.accent)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Notification active in status bar",
                    style = typography.caption,
                    color = colors.accent
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Start / Pause button
        StudyPrimaryButton(
            text = if (state.isPomodoroRunning) "Pause Session" else "Start Session",
            onClick = onToggleRunning,
            leadingIcon = {
                if (state.isPomodoroRunning) {
                    LineIcons.Pause(size = 18.dp, tint = colors.onAccent)
                } else {
                    LineIcons.Play(size = 18.dp, tint = colors.onAccent)
                }
                Spacer(modifier = Modifier.width(8.dp))
            },
            modifier = Modifier.fillMaxWidth(0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        StudyTextButton(
            text = "Reset Timer",
            onClick = onReset,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Keep screen on switch
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onToggleKeepScreenOn() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (state.keepScreenOn) colors.accent else colors.surfaceMuted),
                contentAlignment = Alignment.Center
            ) {
                if (state.keepScreenOn) {
                    LineIcons.Check(size = 12.dp, tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Keep screen awake while running",
                style = typography.caption,
                color = colors.textSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignTopicBottomSheet(
    incompleteTopics: List<Topic>,
    selectedDate: java.time.LocalDate,
    onDismiss: () -> Unit,
    onAssign: (Long) -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .background(colors.divider, CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Assign Topic to ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))}",
                style = typography.heading,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (incompleteTopics.isEmpty()) {
                Text(
                    text = "No unassigned incomplete topics available.",
                    style = typography.body,
                    color = colors.textSecondary
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(incompleteTopics, key = { it.id }) { topic ->
                        StudyCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onAssign(topic.id) },
                            contentPadding = 12.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = topic.name,
                                    style = typography.body,
                                    color = colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                LineIcons.Plus(size = 18.dp, tint = colors.accent)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
