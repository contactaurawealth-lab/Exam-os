package com.studyoffline.app.ui.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.studyoffline.app.data.model.Subject
import com.studyoffline.app.ui.components.*
import com.studyoffline.app.ui.theme.ColorContrastUtil
import com.studyoffline.app.ui.theme.StudyOfflineTheme

@Composable
fun SubjectListScreen(
    viewModel: SubjectsViewModel,
    onNavigateToSubjectDetail: (Long) -> Unit,
    onMenuClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    var showAddSubjectSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            StudyTopBar(
                title = "Subjects",
                onMenuClick = onMenuClick,
                actions = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(onClick = { showAddSubjectSheet = true })
                            .semantics { contentDescription = "Add subject" },
                        contentAlignment = Alignment.Center
                    ) {
                        LineIcons.Plus(size = 24.dp, tint = colors.accent)
                    }
                }
            )

            if (state.subjectsWithProgress.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    StudyEmptyState(
                        title = "No subjects yet",
                        description = "Organize your study goals by adding your first subject.",
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(colors.surfaceMuted, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                LineIcons.Book(size = 32.dp, tint = colors.accent)
                            }
                        },
                        actionButtonText = "Add Subject",
                        onActionClick = { showAddSubjectSheet = true }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.subjectsWithProgress, key = { it.subject.id }) { item ->
                        val percentInt = (item.progressPercent * 100).toInt()
                        val dotColor = ColorContrastUtil.parseHexColor(item.subject.colorHex)

                        StudyCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToSubjectDetail(item.subject.id) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(dotColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.subject.name,
                                        style = typography.subheading,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${item.totalTopics} topic${if (item.totalTopics == 1) "" else "s"} · $percentInt% complete",
                                        style = typography.caption,
                                        color = colors.textSecondary
                                    )
                                }
                                LineIcons.ArrowRight(size = 20.dp, tint = colors.textSecondary)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            StudyLinearProgress(
                                progress = item.progressPercent,
                                color = dotColor
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button (+ Add Subject)
        FloatingActionButton(
            onClick = { showAddSubjectSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
                .semantics { contentDescription = "Add subject" },
            containerColor = colors.accent,
            contentColor = colors.onAccent,
            shape = CircleShape
        ) {
            LineIcons.Plus(size = 24.dp, tint = colors.onAccent)
        }
    }

    if (showAddSubjectSheet) {
        AddEditSubjectBottomSheet(
            onDismiss = { showAddSubjectSheet = false },
            onSave = { name, colorHex ->
                viewModel.addSubject(name, colorHex) {
                    showAddSubjectSheet = false
                }
            }
        )
    }
}
