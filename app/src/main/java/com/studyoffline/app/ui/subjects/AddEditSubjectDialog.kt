package com.studyoffline.app.ui.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.studyoffline.app.data.model.Subject
import com.studyoffline.app.ui.components.*
import com.studyoffline.app.ui.theme.ColorContrastUtil
import com.studyoffline.app.ui.theme.StudyOfflineTheme
import com.studyoffline.app.ui.theme.SubjectTagPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubjectBottomSheet(
    subjectToEdit: Subject? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, colorHex: String) -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    var nameInput by remember { mutableStateOf(subjectToEdit?.name ?: "") }
    var selectedColor by remember { mutableStateOf(subjectToEdit?.colorHex ?: SubjectTagPalette[0]) }

    val nameError = remember(nameInput) {
        val trimmed = nameInput.trim()
        when {
            trimmed.isEmpty() -> "Subject name is required"
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
                text = if (subjectToEdit != null) "Edit Subject" else "New Subject",
                style = typography.heading,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            StudyTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = "Subject Name",
                placeholder = "e.g. Mathematics, Neurobiology",
                errorMessage = if (nameInput.isNotEmpty()) nameError else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tag Color",
                style = typography.caption,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Color picker row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SubjectTagPalette.forEach { hex ->
                    val color = ColorContrastUtil.parseHexColor(hex)
                    val isSelected = selectedColor.equals(hex, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColor = hex }
                            .then(
                                if (isSelected) {
                                    Modifier.border(2.dp, colors.textPrimary, CircleShape)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            LineIcons.Check(size = 16.dp, tint = colors.textPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            StudyPrimaryButton(
                text = if (subjectToEdit != null) "Save Changes" else "Create Subject",
                onClick = {
                    if (isValid) {
                        onSave(nameInput.trim(), selectedColor)
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
