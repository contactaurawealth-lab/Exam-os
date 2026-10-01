package com.studyoffline.app.ui.profile

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyoffline.app.ui.components.*
import com.studyoffline.app.ui.theme.ColorContrastUtil
import com.studyoffline.app.ui.theme.StudyOfflineTheme
import java.time.LocalDate

@Composable
fun ProgressDashboardScreen(
    viewModel: ProfileViewModel,
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null
) {
    val state by viewModel.progressDashboardState.collectAsState()
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        StudyTopBar(
            title = "Progress & Stats",
            onBackClick = onBack,
            onMenuClick = onMenuClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Streak Statistics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Current Streak Card
                StudyCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = 16.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LineIcons.Flame(size = 20.dp, tint = colors.warning)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Current Streak", style = typography.caption, color = colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${state.streakInfo.currentStreak} days",
                        style = typography.displayBold,
                        color = colors.textPrimary
                    )
                }

                // Longest Streak Card
                StudyCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = 16.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LineIcons.Check(size = 20.dp, tint = colors.accent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Longest Streak", style = typography.caption, color = colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${state.streakInfo.longestStreak} days",
                        style = typography.displayBold,
                        color = colors.textPrimary
                    )
                }
            }

            // Weekly Study-Time Bar Chart (Canvas-based)
            StudyCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weekly Study Time",
                        style = typography.subheading,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${state.totalWeeklyMinutes} min total",
                        style = typography.caption,
                        color = colors.accent
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                WeeklyStudyBarChart(
                    data = state.weeklyMinutes,
                    barColor = colors.accent,
                    trackColor = colors.surfaceMuted,
                    labelColor = colors.textSecondary
                )
            }

            // Subject Completion Progress
            StudyCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Subject Completion",
                    style = typography.subheading,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.subjectProgressList.isEmpty()) {
                    Text(
                        text = "No subjects added yet.",
                        style = typography.body,
                        color = colors.textSecondary
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        state.subjectProgressList.forEach { item ->
                            val dotColor = ColorContrastUtil.parseHexColor(item.subject.colorHex)
                            val percentInt = (item.progressPercent * 100).toInt()

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(dotColor, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.subject.name,
                                            style = typography.bodyStrong,
                                            color = colors.textPrimary
                                        )
                                    }

                                    Text(
                                        text = "${item.completedTopics}/${item.totalTopics} ($percentInt%)",
                                        style = typography.caption,
                                        color = colors.textSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                StudyLinearProgress(
                                    progress = item.progressPercent,
                                    color = dotColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyStudyBarChart(
    data: Map<LocalDate, Int>,
    barColor: Color,
    trackColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    val entries = data.entries.toList()
    val maxMinutes = (entries.maxOfOrNull { it.value } ?: 60).coerceAtLeast(60)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            entries.forEach { entry ->
                val ratio = (entry.value.toFloat() / maxMinutes).coerceIn(0f, 1f)
                val dayInitial = entry.key.dayOfWeek.name.take(1)

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (entry.value > 0) {
                        Text(
                            text = "${entry.value}m",
                            style = StudyOfflineTheme.typography.caption.copy(fontSize = 10.sp),
                            color = labelColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Bar Canvas
                    Canvas(
                        modifier = Modifier
                            .width(20.dp)
                            .height(90.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val corner = CornerRadius(6.dp.toPx(), 6.dp.toPx())

                        // Track
                        drawRoundRect(
                            color = trackColor,
                            topLeft = Offset(0f, 0f),
                            size = Size(w, h),
                            cornerRadius = corner
                        )

                        // Filled bar
                        if (ratio > 0f) {
                            val barHeight = h * ratio
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(0f, h - barHeight),
                                size = Size(w, barHeight),
                                cornerRadius = corner
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = dayInitial,
                        style = StudyOfflineTheme.typography.caption,
                        color = labelColor
                    )
                }
            }
        }
    }
}
