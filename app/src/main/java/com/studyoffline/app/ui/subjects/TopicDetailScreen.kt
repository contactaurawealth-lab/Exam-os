package com.studyoffline.app.ui.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.studyoffline.app.data.model.Flashcard
import com.studyoffline.app.data.model.QuizQuestion
import com.studyoffline.app.ui.components.*
import com.studyoffline.app.ui.theme.ColorContrastUtil
import com.studyoffline.app.ui.theme.StudyOfflineTheme

enum class TopicTab(val title: String) {
    NOTES("Notes"),
    FLASHCARDS("Flashcards"),
    QUIZ("Quiz")
}

@Composable
fun TopicDetailScreen(
    topicId: Long,
    viewModel: SubjectsViewModel,
    onBack: () -> Unit,
    onStartFlashcardReview: (topicId: Long) -> Unit,
    onStartTopicQuiz: (topicId: Long) -> Unit
) {
    val topicState by remember(topicId) { viewModel.getTopicDetail(topicId) }.collectAsState(initial = TopicDetailUiState())
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    var selectedTab by remember { mutableStateOf(TopicTab.NOTES) }
    var notesText by remember { mutableStateOf("") }
    var hasNotesChanged by remember { mutableStateOf(false) }

    var showEditTopicSheet by remember { mutableStateOf(false) }
    var showAddFlashcardSheet by remember { mutableStateOf(false) }
    var showAddQuizSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(topicState.topic?.notes) {
        if (!hasNotesChanged) {
            notesText = topicState.topic?.notes ?: ""
        }
    }

    val topic = topicState.topic
    val subject = topicState.subject

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        StudyTopBar(
            title = topic?.name ?: "Topic",
            onBackClick = onBack,
            actions = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { showEditTopicSheet = true },
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

        // Topic Header metadata & completion
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (subject != null) {
                StudyChip(
                    text = subject.name,
                    selected = true,
                    colorDotHex = subject.colorHex,
                    onClick = {}
                )
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            StudyChip(
                text = if (topic?.isCompleted == true) "Completed" else "Mark Complete",
                selected = topic?.isCompleted == true,
                onClick = {
                    if (topic != null) {
                        viewModel.toggleTopicCompletion(topic.id, topic.isCompleted)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Custom Tab Bar with 2dp accent underline (Design Spec §6.5 & §8.3)
        val dividerColor = colors.divider
        val accentColor = colors.accent
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .drawBehind {
                    drawLine(
                        color = dividerColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
        ) {
            TopicTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val textTint = if (isSelected) colors.accent else colors.textSecondary

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { selectedTab = tab }
                        )
                        .drawBehind {
                            if (isSelected) {
                                drawLine(
                                    color = accentColor,
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.title.uppercase(),
                        style = typography.label,
                        color = textTint
                    )
                }
            }
        }

        // Lazy-loaded Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                TopicTab.NOTES -> {
                    NotesTabContent(
                        notes = notesText,
                        onNotesChange = {
                            notesText = it
                            hasNotesChanged = true
                        },
                        onSaveNotes = {
                            viewModel.saveNotes(topicId, notesText)
                            hasNotesChanged = false
                        },
                        hasChanges = hasNotesChanged
                    )
                }
                TopicTab.FLASHCARDS -> {
                    FlashcardsTabContent(
                        flashcards = topicState.flashcards,
                        onAddCard = { showAddFlashcardSheet = true },
                        onDeleteCard = { viewModel.deleteFlashcard(it) },
                        onStartReview = { onStartFlashcardReview(topicId) }
                    )
                }
                TopicTab.QUIZ -> {
                    QuizTabContent(
                        questions = topicState.quizQuestions,
                        onAddQuestion = { showAddQuizSheet = true },
                        onDeleteQuestion = { viewModel.deleteQuizQuestion(it) },
                        onStartQuiz = { onStartTopicQuiz(topicId) }
                    )
                }
            }
        }
    }

    if (showEditTopicSheet && topic != null) {
        AddEditTopicBottomSheet(
            topicToEdit = topic,
            onDismiss = { showEditTopicSheet = false },
            onSave = { name ->
                viewModel.updateTopic(topic.copy(name = name)) {
                    showEditTopicSheet = false
                }
            }
        )
    }

    if (showAddFlashcardSheet) {
        AddFlashcardBottomSheet(
            onDismiss = { showAddFlashcardSheet = false },
            onSave = { front, back ->
                viewModel.addFlashcard(topicId, front, back) {
                    showAddFlashcardSheet = false
                }
            }
        )
    }

    if (showAddQuizSheet) {
        AddQuizQuestionBottomSheet(
            onDismiss = { showAddQuizSheet = false },
            onSave = { question, options, correctIndex ->
                viewModel.addQuizQuestion(topicId, question, options, correctIndex) {
                    showAddQuizSheet = false
                }
            }
        )
    }

    if (showDeleteConfirmDialog && topic != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(text = "Delete Topic?", style = typography.subheading, color = colors.textPrimary)
            },
            text = {
                Text(
                    text = "This will delete '${topic.name}' along with its notes, flashcards, and quiz questions.",
                    style = typography.body,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                StudyDestructiveButton(
                    text = "Delete",
                    onClick = {
                        viewModel.deleteTopic(topicId) {
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

@Composable
private fun NotesTabContent(
    notes: String,
    onNotesChange: (String) -> Unit,
    onSaveNotes: () -> Unit,
    hasChanges: Boolean
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Study Notes & Summary",
                style = typography.caption,
                color = colors.textSecondary
            )
            if (hasChanges) {
                StudyTextButton(
                    text = "Save",
                    onClick = onSaveNotes
                )
            } else {
                Text(
                    text = "Saved",
                    style = typography.caption,
                    color = colors.accent
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        StudyTextField(
            value = notes,
            onValueChange = onNotesChange,
            placeholder = "Write your notes, key formulas, conceptual definitions here...",
            singleLine = false,
            maxLines = 20,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FlashcardsTabContent(
    flashcards: List<Flashcard>,
    onAddCard: () -> Unit,
    onDeleteCard: (Long) -> Unit,
    onStartReview: () -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${flashcards.size} flashcards",
                style = typography.caption,
                color = colors.textSecondary
            )

            Row {
                if (flashcards.isNotEmpty()) {
                    StudySecondaryButton(
                        text = "Review",
                        onClick = onStartReview
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                StudyPrimaryButton(
                    text = "+ Add",
                    onClick = onAddCard
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (flashcards.isEmpty()) {
            StudyEmptyState(
                title = "No flashcards yet",
                description = "Add question and answer cards to leverage spaced repetition review.",
                icon = {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(colors.surfaceMuted, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        LineIcons.Practice(size = 28.dp, tint = colors.accent)
                    }
                },
                actionButtonText = "Add Flashcard",
                onActionClick = onAddCard
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(flashcards, key = { it.id }) { card ->
                    StudyCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Q: ${card.front}",
                                    style = typography.bodyStrong,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "A: ${card.back}",
                                    style = typography.body,
                                    color = colors.textSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { onDeleteCard(card.id) },
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
private fun QuizTabContent(
    questions: List<QuizQuestion>,
    onAddQuestion: () -> Unit,
    onDeleteQuestion: (Long) -> Unit,
    onStartQuiz: () -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${questions.size} questions",
                style = typography.caption,
                color = colors.textSecondary
            )

            Row {
                if (questions.isNotEmpty()) {
                    StudySecondaryButton(
                        text = "Take Quiz",
                        onClick = onStartQuiz
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                StudyPrimaryButton(
                    text = "+ Add",
                    onClick = onAddQuestion
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (questions.isEmpty()) {
            StudyEmptyState(
                title = "No quiz questions yet",
                description = "Create multiple choice questions to test your comprehension.",
                icon = {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(colors.surfaceMuted, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        LineIcons.Check(size = 28.dp, tint = colors.accent)
                    }
                },
                actionButtonText = "Add Question",
                onActionClick = onAddQuestion
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(questions, key = { it.id }) { q ->
                    StudyCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = q.question,
                                    style = typography.bodyStrong,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                q.options.forEachIndexed { idx, opt ->
                                    val isCorrect = idx == q.correctIndex
                                    Text(
                                        text = "${idx + 1}. $opt${if (isCorrect) " (Correct)" else ""}",
                                        style = typography.caption,
                                        color = if (isCorrect) colors.success else colors.textSecondary
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { onDeleteQuestion(q.id) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFlashcardBottomSheet(
    onDismiss: () -> Unit,
    onSave: (front: String, back: String) -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    var frontInput by remember { mutableStateOf("") }
    var backInput by remember { mutableStateOf("") }

    val isValid = frontInput.trim().isNotEmpty() && backInput.trim().isNotEmpty()

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
                text = "New Flashcard",
                style = typography.heading,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            StudyTextField(
                value = frontInput,
                onValueChange = { frontInput = it },
                label = "Front (Prompt / Question)",
                placeholder = "e.g. What is the First Law of Thermodynamics?",
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(14.dp))

            StudyTextField(
                value = backInput,
                onValueChange = { backInput = it },
                label = "Back (Answer / Definition)",
                placeholder = "e.g. Energy cannot be created or destroyed, only transformed.",
                singleLine = false,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            StudyPrimaryButton(
                text = "Add Card",
                onClick = {
                    if (isValid) {
                        onSave(frontInput.trim(), backInput.trim())
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuizQuestionBottomSheet(
    onDismiss: () -> Unit,
    onSave: (question: String, options: List<String>, correctIndex: Int) -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    var questionInput by remember { mutableStateOf("") }
    var opt1 by remember { mutableStateOf("") }
    var opt2 by remember { mutableStateOf("") }
    var opt3 by remember { mutableStateOf("") }
    var opt4 by remember { mutableStateOf("") }
    var correctIndex by remember { mutableStateOf(0) }

    val options = listOf(opt1, opt2, opt3, opt4)
    val isValid = questionInput.trim().isNotEmpty() && options.all { it.trim().isNotEmpty() }

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
                text = "New Quiz Question",
                style = typography.heading,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            StudyTextField(
                value = questionInput,
                onValueChange = { questionInput = it },
                label = "Question",
                placeholder = "e.g. What is the powerhouse of the cell?"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Answer Options (tap bullet to mark correct)",
                style = typography.caption,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            listOf(
                opt1 to { v: String -> opt1 = v },
                opt2 to { v: String -> opt2 = v },
                opt3 to { v: String -> opt3 = v },
                opt4 to { v: String -> opt4 = v }
            ).forEachIndexed { index, (value, setter) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (correctIndex == index) colors.success else colors.surfaceMuted)
                            .clickable { correctIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        if (correctIndex == index) {
                            LineIcons.Check(size = 14.dp, tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    StudyTextField(
                        value = value,
                        onValueChange = setter,
                        placeholder = "Option ${index + 1}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            StudyPrimaryButton(
                text = "Add Question",
                onClick = {
                    if (isValid) {
                        onSave(questionInput.trim(), options.map { it.trim() }, correctIndex)
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
