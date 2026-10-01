package com.studyoffline.app.ui.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studyoffline.app.ui.components.*
import com.studyoffline.app.ui.theme.StudyOfflineTheme

@Composable
fun PracticeHomeScreen(
    viewModel: PracticeViewModel,
    onNavigateToQuizSetup: () -> Unit,
    onNavigateToFlashcardReview: () -> Unit,
    onNavigateToWeakQuestions: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    val state by viewModel.homeUiState.collectAsState()
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        StudyTopBar(
            title = "Practice",
            onMenuClick = onMenuClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Quiz Mode
            StudyCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToQuizSetup
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(colors.surfaceSelected, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        LineIcons.Check(size = 24.dp, tint = colors.accent)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Quiz",
                            style = typography.subheading,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Test your knowledge with multiple choice sessions",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }

                    LineIcons.ArrowRight(size = 20.dp, tint = colors.textSecondary)
                }
            }

            // Card 2: Flashcard Review
            StudyCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToFlashcardReview,
                backgroundColor = if (state.dueFlashcardCount > 0) colors.surfaceSelected else colors.surface,
                borderColor = if (state.dueFlashcardCount > 0) colors.accent.copy(alpha = 0.5f) else colors.divider
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(if (state.dueFlashcardCount > 0) colors.surface else colors.surfaceMuted, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        LineIcons.Practice(size = 24.dp, tint = colors.accent)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Flashcard Review",
                                style = typography.subheading,
                                color = colors.textPrimary
                            )
                            if (state.dueFlashcardCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                StudyChip(
                                    text = "${state.dueFlashcardCount} due",
                                    selected = true,
                                    onClick = {}
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (state.dueFlashcardCount > 0) {
                                "Cards scheduled for spaced repetition review today"
                            } else {
                                "All caught up! Tap to review anyway"
                            },
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }

                    LineIcons.ArrowRight(size = 20.dp, tint = colors.textSecondary)
                }
            }

            // Card 3: Weak Topics
            StudyCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToWeakQuestions
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(colors.surfaceMuted, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        LineIcons.Flag(size = 22.dp, tint = if (state.weakQuestionCount > 0) colors.warning else colors.textSecondary)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Weak Topics",
                                style = typography.subheading,
                                color = colors.textPrimary
                            )
                            if (state.weakQuestionCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(${state.weakQuestionCount} flagged)",
                                    style = typography.caption,
                                    color = colors.warning
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Target questions you previously missed or marked for improvement",
                            style = typography.caption,
                            color = colors.textSecondary
                        )
                    }

                    LineIcons.ArrowRight(size = 20.dp, tint = colors.textSecondary)
                }
            }
        }
    }
}
