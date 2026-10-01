package com.studyoffline.app.ui.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.studyoffline.app.data.model.Subject
import com.studyoffline.app.data.model.Topic
import com.studyoffline.app.ui.components.*
import com.studyoffline.app.ui.theme.ColorContrastUtil
import com.studyoffline.app.ui.theme.StudyOfflineTheme

@Composable
fun SubjectDetailScreen(
    subjectId: Long,
    viewModel: SubjectsViewModel,
    onBack: () -> Unit,
    onNavigateToTopicDetail: (Long) -> Unit
) {
    val subject by viewModel.getSubject(subjectId).collectAsState(initial = null)
    val topics by viewModel.getTopicsForSubject(subjectId).collectAsState(initial = emptyList())

    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    var showAddTopicSheet by remember { mutableStateOf(false) }
    var showEditSubjectSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val totalTopics = topics.size
    val completedTopics = topics.count { it.isCompleted }
    val progress = if (totalTopics > 0) completedTopics.toFloat() / totalTopics else 0f
    val percentInt = (progress * 100).toInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            StudyTopBar(
                title = subject?.name ?: "Subject",
                onBackClick = onBack,
                actions = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { showEditSubjectSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        LineIcons.Settings(size = 20.dp, tint = colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { showDeleteConfirmDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        LineIcons.Trash(size = 20.dp, tint = colors.error)
                    }
                }
            )

            // Progress Header Card
            StudyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Subject Completion",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "$completedTopics of $totalTopics completed ($percentInt%)",
                        style = typography.caption,
                        color = colors.accent
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val dotColor = ColorContrastUtil.parseHexColor(subject?.colorHex ?: "#7C9A82")
                StudyLinearProgress(
                    progress = progress,
                    color = dotColor
                )
            }

            // Topics List
            if (topics.isEmpty()) {
                StudyEmptyState(
                    title = "No topics added yet",
                    description = "Break this subject down into study topics, notes, and flashcards.",
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
                    actionButtonText = "Add Topic",
                    onActionClick = { showAddTopicSheet = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(topics, key = { it.id }) { topic ->
                        StudyCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToTopicDetail(topic.id) },
                            contentPadding = 14.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Completion toggle
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
                                        .clickable {
                                            viewModel.toggleTopicCompletion(topic.id, topic.isCompleted)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (topic.isCompleted) {
                                        LineIcons.Check(size = 14.dp, tint = colors.success)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = topic.name,
                                        style = typography.body.copy(
                                            color = if (topic.isCompleted) colors.textSecondary else colors.textPrimary
                                        )
                                    )
                                    if (!topic.notes.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Has notes",
                                            style = typography.caption,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                LineIcons.ArrowRight(size = 18.dp, tint = colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }

        // FAB (+ Add Topic)
        FloatingActionButton(
            onClick = { showAddTopicSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
                .semantics { contentDescription = "Add topic" },
            containerColor = colors.accent,
            contentColor = colors.onAccent,
            shape = CircleShape
        ) {
            LineIcons.Plus(size = 24.dp, tint = colors.onAccent)
        }
    }

    if (showAddTopicSheet) {
        AddEditTopicBottomSheet(
            onDismiss = { showAddTopicSheet = false },
            onSave = { name ->
                viewModel.addTopic(subjectId, name) {
                    showAddTopicSheet = false
                }
            }
        )
    }

    if (showEditSubjectSheet && subject != null) {
        AddEditSubjectBottomSheet(
            subjectToEdit = subject,
            onDismiss = { showEditSubjectSheet = false },
            onSave = { name, colorHex ->
                viewModel.updateSubject(subject!!.copy(name = name, colorHex = colorHex)) {
                    showEditSubjectSheet = false
                }
            }
        )
    }

    if (showDeleteConfirmDialog && subject != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(text = "Delete Subject?", style = typography.subheading, color = colors.textPrimary)
            },
            text = {
                Text(
                    text = "This will permanently remove '${subject?.name}' and all associated topics, notes, and flashcards.",
                    style = typography.body,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                StudyDestructiveButton(
                    text = "Delete",
                    onClick = {
                        viewModel.deleteSubject(subjectId) {
                            showDeleteConfirmDialog = false
                            onBack()
                        }
                    }
                )
            },
            dismissButton = {
                StudyTextButton(
                    text = "Cancel",
                    onClick = { showDeleteConfirmDialog = false },
                    color = colors.textSecondary
                )
            },
            containerColor = colors.surface
        )
    }
}
