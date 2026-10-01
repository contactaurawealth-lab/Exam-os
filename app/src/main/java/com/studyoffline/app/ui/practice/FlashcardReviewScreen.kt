package com.studyoffline.app.ui.practice

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.studyoffline.app.ui.components.*
import com.studyoffline.app.ui.theme.StudyOfflineTheme

@Composable
fun FlashcardReviewScreen(
    viewModel: PracticeViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.flashcardState.collectAsState()
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

    if (state.cards.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            StudyEmptyState(
                title = "No flashcards due",
                description = "All caught up on your spaced repetition reviews!",
                actionButtonText = "Done",
                onActionClick = onBack
            )
        }
        return
    }

    if (state.isCompleted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(colors.surfaceSelected, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                LineIcons.Check(size = 32.dp, tint = colors.accent)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Review Complete",
                style = typography.heading,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You reviewed ${state.reviewedCount} cards. Spaced repetition intervals have been recalculated.",
                style = typography.body,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            StudyPrimaryButton(
                text = "Done",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
        return
    }

    val currentCard = state.cards[state.currentIndex]
    val totalCards = state.cards.size
    val progress = (state.currentIndex + 1).toFloat() / totalCards

    // 3D Flip animation
    val rotation by animateFloatAsState(
        targetValue = if (state.isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "FlashcardRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        StudyTopBar(
            title = "Card ${state.currentIndex + 1} of $totalCards",
            onBackClick = onBack
        )

        StudyLinearProgress(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (state.isFlipped) "Answer (tap card to flip back)" else "Prompt (tap card to reveal answer)",
                style = typography.caption,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Centered 4:3 Flashcard
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { viewModel.flipFlashcard() }
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Front / Back text
                if (rotation <= 90f) {
                    Text(
                        text = currentCard.front,
                        style = typography.subheading,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = currentCard.back,
                        style = typography.subheading,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer {
                            rotationY = 180f // Un-mirror back content
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SM-2 Review Grade buttons (Again, Hard, Good, Easy)
            // PRD §5.4 & design.md §8.5: Sized equally, 8dp gaps
            if (state.isFlipped) {
                Text(
                    text = "How well did you remember this?",
                    style = typography.caption,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 0: Again (error outline)
                    OutlinedButton(
                        onClick = { viewModel.reviewFlashcard(0) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, colors.error),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "Again", style = typography.caption)
                    }

                    // 1: Hard (warning outline)
                    OutlinedButton(
                        onClick = { viewModel.reviewFlashcard(1) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, colors.warning),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.warning),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "Hard", style = typography.caption)
                    }

                    // 2: Good (accent outline)
                    OutlinedButton(
                        onClick = { viewModel.reviewFlashcard(2) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, colors.accent),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "Good", style = typography.caption)
                    }

                    // 3: Easy (accent fill)
                    Button(
                        onClick = { viewModel.reviewFlashcard(3) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.onAccent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "Easy", style = typography.caption)
                    }
                }
            } else {
                StudySecondaryButton(
                    text = "Show Answer",
                    onClick = { viewModel.flipFlashcard() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
