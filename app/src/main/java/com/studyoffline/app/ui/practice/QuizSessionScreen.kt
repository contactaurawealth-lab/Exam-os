package com.studyoffline.app.ui.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.studyoffline.app.ui.components.*
import com.studyoffline.app.ui.theme.StudyOfflineTheme

@Composable
fun QuizSessionScreen(
    viewModel: PracticeViewModel,
    onBack: () -> Unit,
    onQuizComplete: () -> Unit
) {
    val state by viewModel.quizState.collectAsState()
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colors.accent)
        }
        return
    }

    if (state.questions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            StudyEmptyState(
                title = "No questions found",
                description = "There are no quiz questions matching your selection.",
                actionButtonText = "Go Back",
                onActionClick = onBack
            )
        }
        return
    }

    if (state.isCompleted) {
        onQuizComplete()
        return
    }

    val currentQuestion = state.questions[state.currentIndex]
    val totalCount = state.questions.size
    val progress = (state.currentIndex + 1).toFloat() / totalCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        StudyTopBar(
            title = "Question ${state.currentIndex + 1} of $totalCount",
            onBackClick = onBack
        )

        // Progress bar
        StudyLinearProgress(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Question Card
            StudyCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 20.dp
            ) {
                Text(
                    text = currentQuestion.question,
                    style = typography.subheading,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Options stacked cards (12dp gap)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                currentQuestion.options.forEachIndexed { index, optionText ->
                    val isSelected = state.selectedOptionIndex == index
                    val isCorrect = index == currentQuestion.correctIndex

                    // Calm borders on answer checking:
                    // If answer checked:
                    // - If selected and correct -> success border & check icon
                    // - If selected and wrong -> error border & close icon
                    // - If not selected but correct -> success border & check icon
                    // - Background stays neutral!
                    val borderColor = when {
                        !state.isAnswerChecked && isSelected -> colors.accent
                        state.isAnswerChecked && isCorrect -> colors.success
                        state.isAnswerChecked && isSelected && !isCorrect -> colors.error
                        else -> colors.divider
                    }

                    StudyCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = borderColor,
                        onClick = {
                            if (!state.isAnswerChecked) {
                                viewModel.selectQuizOption(index)
                            }
                        },
                        contentPadding = 16.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.isAnswerChecked) {
                                if (isCorrect) {
                                    LineIcons.Check(size = 20.dp, tint = colors.success)
                                    Spacer(modifier = Modifier.width(12.dp))
                                } else if (isSelected) {
                                    LineIcons.Close(size = 20.dp, tint = colors.error)
                                    Spacer(modifier = Modifier.width(12.dp))
                                } else {
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) colors.accent else colors.surfaceMuted),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(colors.onAccent, CircleShape)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            Text(
                                text = optionText,
                                style = typography.body,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(28.dp))

            // Action Button
            if (!state.isAnswerChecked) {
                StudyPrimaryButton(
                    text = "Check Answer",
                    onClick = { viewModel.checkQuizAnswer() },
                    enabled = state.selectedOptionIndex != null,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                val isLast = state.currentIndex + 1 >= totalCount
                StudyPrimaryButton(
                    text = if (isLast) "See Results" else "Next Question",
                    onClick = { viewModel.nextQuizQuestion() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
