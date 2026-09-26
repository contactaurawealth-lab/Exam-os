package com.studyoffline.app.ui.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import com.studyoffline.app.ui.components.*
import com.studyoffline.app.ui.theme.ColorContrastUtil
import com.studyoffline.app.ui.theme.StudyOfflineTheme

@Composable
fun QuizSetupScreen(
    viewModel: PracticeViewModel,
    onBack: () -> Unit,
    onStartSession: () -> Unit,
    initialSubjectId: Long? = null,
    isWeakOnly: Boolean = false
) {
    val state by viewModel.homeUiState.collectAsState()
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    var selectedSubjectId by remember { mutableStateOf<Long?>(initialSubjectId) }
    var selectedCount by remember { mutableStateOf(10) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        StudyTopBar(
            title = if (isWeakOnly) "Weak Topics Quiz" else "Quiz Setup",
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            if (!isWeakOnly) {
                Text(
                    text = "Select Subject",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // "All Subjects" chip
                StudyChip(
                    text = "All Subjects",
                    selected = selectedSubjectId == null,
                    onClick = { selectedSubjectId = null }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subject list
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.subjects.forEach { subject ->
                        val isSelected = selectedSubjectId == subject.id
                        StudyCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { selectedSubjectId = subject.id },
                            backgroundColor = if (isSelected) colors.surfaceSelected else colors.surface,
                            borderColor = if (isSelected) colors.accent else colors.divider,
                            contentPadding = 12.dp
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val dotColor = ColorContrastUtil.parseHexColor(subject.colorHex)
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(dotColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = subject.name,
                                    style = typography.body,
                                    color = colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    LineIcons.Check(size = 18.dp, tint = colors.accent)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Question count selector
            Text(
                text = "Question Count",
                style = typography.caption,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 10, 20, 0).forEach { count ->
                    val isSelected = selectedCount == count
                    val label = if (count == 0) "All" else "$count"
                    StudyChip(
                        text = label,
                        selected = isSelected,
                        onClick = { selectedCount = count },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            StudyPrimaryButton(
                text = "Start Quiz",
                onClick = {
                    viewModel.startQuiz(selectedSubjectId, selectedCount, isWeakOnly)
                    onStartSession()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
