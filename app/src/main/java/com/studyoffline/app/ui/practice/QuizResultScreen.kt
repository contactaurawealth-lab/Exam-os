package com.studyoffline.app.ui.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun QuizResultScreen(
    viewModel: PracticeViewModel,
    onDone: () -> Unit
) {
    val state by viewModel.quizState.collectAsState()
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    val total = state.questions.size
    val correctCount = state.questions.indices.count { idx ->
        state.userAnswers[idx] == state.questions[idx].correctIndex
    }
    val scorePercent = if (total > 0) ((correctCount.toFloat() / total) * 100).toInt() else 0

    val missedQuestions = state.questions.filterIndexed { idx, q ->
        state.userAnswers[idx] != q.correctIndex
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        StudyTopBar(
            title = "Quiz Results",
            onBackClick = onDone
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Score Summary Card
            item {
                StudyCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 24.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        StudyCircularProgress(
                            progress = correctCount.toFloat() / (if (total > 0) total else 1),
                            size = 120.dp,
                            strokeWidth = 6.dp,
                            progressColor = if (scorePercent >= 70) colors.success else colors.warning
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$scorePercent%",
                                    style = typography.displayBold,
                                    color = colors.textPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Score: $correctCount / $total correct",
                            style = typography.heading,
                            color = colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = when {
                                scorePercent >= 90 -> "Outstanding recall! Keep up the consistency."
                                scorePercent >= 70 -> "Good work! Review missed items below."
                                else -> "Needs attention. Flag missed items to practice again."
                            },
                            style = typography.body,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            if (missedQuestions.isNotEmpty()) {
                item {
                    Text(
                        text = "Missed Questions (${missedQuestions.size})",
                        style = typography.subheading,
                        color = colors.textPrimary
                    )
                }

                items(missedQuestions, key = { it.id }) { q ->
                    val userOptionIdx = state.questions.indexOf(q).let { state.userAnswers[it] }
                    StudyCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = colors.error.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = q.question,
                            style = typography.bodyStrong,
                            color = colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (userOptionIdx != null && userOptionIdx in q.options.indices) {
                            Text(
                                text = "Your answer: ${q.options[userOptionIdx]}",
                                style = typography.caption,
                                color = colors.error
                            )
                        }

                        Text(
                            text = "Correct answer: ${q.options.getOrNull(q.correctIndex) ?: ""}",
                            style = typography.caption,
                            color = colors.success
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StudyChip(
                                text = if (q.isWeak) "In Weak Topics" else "Flag as Weak Topic",
                                selected = q.isWeak,
                                onClick = {
                                    viewModel.toggleWeakStatus(q.id, q.isWeak)
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                StudyPrimaryButton(
                    text = "Done",
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
