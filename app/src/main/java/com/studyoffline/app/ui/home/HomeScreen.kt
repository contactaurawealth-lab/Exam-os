package com.studyoffline.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyoffline.app.ui.components.*
import com.studyoffline.app.ui.theme.ColorContrastUtil
import com.studyoffline.app.ui.theme.StudyOfflineTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToTopic: (Long) -> Unit,
    onNavigateToCountdownDetail: () -> Unit,
    onNavigateToFlashcardsDue: () -> Unit,
    onNavigateToAddSubject: () -> Unit,
    onNavigateToPlanner: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top row: Greeting + Streak counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = state.greeting,
                    style = typography.subheading,
                    color = colors.textSecondary
                )
                Text(
                    text = "Ready to study?",
                    style = typography.heading,
                    color = colors.textPrimary
                )
            }

            // Streak Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceMuted)
                    .border(1.dp, colors.divider, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LineIcons.Flame(size = 20.dp, tint = if (state.streakInfo.currentStreak > 0) colors.warning else colors.textSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${state.streakInfo.currentStreak}",
                    style = typography.bodyStrong.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "day${if (state.streakInfo.currentStreak == 1) "" else "s"}",
                    style = typography.caption,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Empty state check
        if (state.subjectCount == 0 && !state.isLoading) {
            StudyEmptyState(
                title = "No subjects added yet",
                description = "Add your first subject to start building topics, flashcards, and quizzes.",
                icon = {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(colors.surfaceMuted, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        LineIcons.Book(size = 32.dp, tint = colors.accent)
                    }
                },
                actionButtonText = "Add Subject",
                onActionClick = onNavigateToAddSubject
            )
            return
        }

        // Countdown Card (if goal set)
        if (state.daysRemaining != null) {
            StudyCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToCountdownDetail
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.examName.isNotEmpty()) state.examName else "Target Exam",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${state.daysRemaining}",
                                style = typography.displayBold,
                                color = colors.accent
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "days remaining",
                                style = typography.body,
                                color = colors.textPrimary
                            )
                        }
                    }

                    // Mini circular progress
                    StudyCircularProgress(
                        progress = (100 - (state.daysRemaining ?: 0).coerceIn(0, 100)) / 100f,
                        size = 52.dp,
                        strokeWidth = 4.dp
                    ) {
                        LineIcons.Timer(size = 20.dp, tint = colors.accent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Spaced Repetition Due Alert
        if (state.dueFlashcardCount > 0) {
            StudyCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceSelected,
                borderColor = colors.accent.copy(alpha = 0.5f),
                onClick = onNavigateToFlashcardsDue
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        LineIcons.Practice(size = 22.dp, tint = colors.accent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${state.dueFlashcardCount} flashcard${if (state.dueFlashcardCount == 1) "" else "s"} due for review",
                                style = typography.bodyStrong,
                                color = colors.accent
                            )
                            Text(
                                text = "Spaced repetition reminder",
                                style = typography.caption,
                                color = colors.textSecondary
                            )
                        }
                    }
                    LineIcons.ArrowRight(size = 20.dp, tint = colors.accent)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Today's Plan section (§7.3)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Plan",
                style = typography.subheading,
                color = colors.textPrimary
            )
            Text(
                text = "${state.todayPlanItems.size} scheduled",
                style = typography.caption,
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (state.todayPlanItems.isEmpty()) {
            StudyCard(modifier = Modifier.fillMaxWidth()) {
                val isNewUser = state.subjectCount == 0
                val hasNoTopics = state.totalTopicsCount == 0
                val isActuallyCaughtUp = state.completedTopicsCount > 0 && state.streakInfo.currentStreak > 0

                val emptyTitle = when {
                    isNewUser -> "Start your study plan"
                    hasNoTopics -> "Add topics to study"
                    isActuallyCaughtUp -> "You're all caught up for today! 🎉"
                    else -> "No topics scheduled for today"
                }

                val emptyDescription = when {
                    isNewUser -> "Create your first subject and topics to start scheduling revision."
                    hasNoTopics -> "Add revision topics under your subjects to track them here."
                    isActuallyCaughtUp -> "Great job completing your revision goals! Head to Planner if you'd like to schedule extra sessions."
                    else -> "Plan your daily study sessions in the Planner to build your revision streak."
                }

                Text(
                    text = emptyTitle,
                    style = typography.subheading,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = emptyDescription,
                    style = typography.caption,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isNewUser || hasNoTopics) {
                        StudyPrimaryButton(
                            text = if (isNewUser) "Add Subject" else "View Subjects",
                            onClick = onNavigateToAddSubject,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        StudyPrimaryButton(
                            text = "Open Planner",
                            onClick = onNavigateToPlanner,
                            modifier = Modifier.weight(1f)
                        )
                        StudySecondaryButton(
                            text = "View Subjects",
                            onClick = onNavigateToAddSubject,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.todayPlanItems.forEach { planItem ->
                    val isCompleted = planItem.topic.isCompleted
                    StudyCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigateToTopic(planItem.topic.id) },
                        contentPadding = 12.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Checkbox tap
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(
                                        1.5.dp,
                                        if (isCompleted) colors.success else colors.divider,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .background(
                                        if (isCompleted) colors.success.copy(alpha = 0.15f) else Color.Transparent
                                    )
                                    .clickable {
                                        viewModel.toggleTopicCompletion(planItem.topic.id, isCompleted)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    LineIcons.Check(size = 14.dp, tint = colors.success)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Subject tag dot
                            val dotColor = ColorContrastUtil.parseHexColor(planItem.subjectColorHex)
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(dotColor, CircleShape)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = planItem.topic.name,
                                    style = typography.body.copy(
                                        color = if (isCompleted) colors.textSecondary else colors.textPrimary
                                    )
                                )
                                Text(
                                    text = planItem.subjectName,
                                    style = typography.caption,
                                    color = colors.textSecondary
                                )
                            }

                            LineIcons.ArrowRight(size = 18.dp, tint = colors.textSecondary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
