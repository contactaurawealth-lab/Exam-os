package com.studyoffline.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.studyoffline.app.ui.theme.StudyOfflineTheme

@Composable
fun StudyPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.accent,
            contentColor = colors.onAccent,
            disabledContainerColor = colors.accent.copy(alpha = 0.4f),
            disabledContentColor = colors.textDisabled
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (leadingIcon != null) {
            leadingIcon()
        }
        Text(
            text = text,
            style = typography.bodyStrong
        )
    }
}

@Composable
fun StudySecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (enabled) colors.divider else colors.divider.copy(alpha = 0.4f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = colors.textPrimary,
            disabledContentColor = colors.textDisabled
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (leadingIcon != null) {
            leadingIcon()
        }
        Text(
            text = text,
            style = typography.bodyStrong
        )
    }
}

@Composable
fun StudyTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = StudyOfflineTheme.colors.accent
) {
    val typography = StudyOfflineTheme.typography

    TextButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = color,
            disabledContentColor = StudyOfflineTheme.colors.textDisabled
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            style = typography.bodyStrong
        )
    }
}

@Composable
fun StudyDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (enabled) colors.error else colors.error.copy(alpha = 0.4f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = colors.error,
            disabledContentColor = colors.textDisabled
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            style = typography.bodyStrong
        )
    }
}
