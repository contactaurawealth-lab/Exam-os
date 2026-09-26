package com.studyoffline.app.ui.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studyoffline.app.data.model.Topic
import com.studyoffline.app.ui.components.*
import com.studyoffline.app.ui.theme.StudyOfflineTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTopicBottomSheet(
    topicToEdit: Topic? = null,
    onDismiss: () -> Unit,
    onSave: (name: String) -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    var nameInput by remember { mutableStateOf(topicToEdit?.name ?: "") }

    val nameError = remember(nameInput) {
        val trimmed = nameInput.trim()
        when {
            trimmed.isEmpty() -> "Topic name is required"
            trimmed.length > 60 -> "Name must be 60 characters or less (${trimmed.length}/60)"
            else -> null
        }
    }

    val isValid = nameError == null && nameInput.trim().isNotEmpty()

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
                text = if (topicToEdit != null) "Edit Topic" else "New Topic",
                style = typography.heading,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            StudyTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = "Topic Name",
                placeholder = "e.g. Organic Reactions, Thermodynamics",
                errorMessage = if (nameInput.isNotEmpty()) nameError else null
            )

            Spacer(modifier = Modifier.height(28.dp))

            StudyPrimaryButton(
                text = if (topicToEdit != null) "Save Changes" else "Create Topic",
                onClick = {
                    if (isValid) {
                        onSave(nameInput.trim())
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
