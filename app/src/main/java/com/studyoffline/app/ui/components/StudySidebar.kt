package com.studyoffline.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyoffline.app.ui.theme.StudyOfflineTheme

data class SidebarNavigationItem(
    val route: String,
    val title: String,
    val subtitle: String? = null,
    val section: String,
    val badge: String? = null,
    val icon: @Composable (Color) -> Unit
)

@Composable
fun StudySidebar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    streakDays: Int = 0,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    val items = listOf(
        // Section: OVERVIEW
        SidebarNavigationItem(
            route = "main_tab/HOME",
            title = "Home",
            subtitle = "Dashboard & Daily Target",
            section = "MAIN",
            icon = { tint -> LineIcons.Home(size = 22.dp, tint = tint) }
        ),
        SidebarNavigationItem(
            route = "main_tab/SUBJECTS",
            title = "Subjects",
            subtitle = "Syllabi & Topic Mastery",
            section = "MAIN",
            icon = { tint -> LineIcons.Book(size = 22.dp, tint = tint) }
        ),
        SidebarNavigationItem(
            route = "main_tab/PRACTICE",
            title = "Practice",
            subtitle = "Flashcards & Quick Quizzes",
            section = "MAIN",
            icon = { tint -> LineIcons.Practice(size = 22.dp, tint = tint) }
        ),

        // Section: FOCUS & TOOLS
        SidebarNavigationItem(
            route = "main_tab/POMODORO",
            title = "Focus Timer",
            subtitle = "Pomodoro & Deep Work",
            section = "FOCUS & TOOLS",
            badge = "Timer",
            icon = { tint -> LineIcons.Timer(size = 22.dp, tint = tint) }
        ),
        SidebarNavigationItem(
            route = "main_tab/BLOCKER",
            title = "App Blocker",
            subtitle = "Passcode & 5-Min Grace",
            section = "FOCUS & TOOLS",
            badge = "5-min lock",
            icon = { tint -> LineIcons.Lock(size = 22.dp, tint = tint) }
        ),
        SidebarNavigationItem(
            route = "main_tab/PLANNER",
            title = "Study Planner",
            subtitle = "Calendar & Revision Schedule",
            section = "FOCUS & TOOLS",
            icon = { tint -> LineIcons.Calendar(size = 22.dp, tint = tint) }
        ),

        // Section: INSIGHTS & CONFIG
        SidebarNavigationItem(
            route = "main_tab/PROFILE",
            title = "Progress & Stats",
            subtitle = "Analytics & Retention Rates",
            section = "INSIGHTS & CONFIG",
            icon = { tint -> LineIcons.User(size = 22.dp, tint = tint) }
        ),
        SidebarNavigationItem(
            route = "settings",
            title = "Settings",
            subtitle = "Goals, Themes & Reminders",
            section = "INSIGHTS & CONFIG",
            icon = { tint -> LineIcons.Settings(size = 22.dp, tint = tint) }
        )
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(colors.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = 12.dp)
    ) {
        // --- Drawer Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceSelected)
                        .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    LineIcons.Book(size = 24.dp, tint = colors.accent)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "StudyOffline",
                        style = typography.heading,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Distraction-Free Companion",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                LineIcons.Close(size = 20.dp, tint = colors.textSecondary)
            }
        }

        // Streak Banner in Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceMuted)
                .border(1.dp, colors.divider, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LineIcons.Flame(
                        size = 18.dp,
                        tint = if (streakDays > 0) colors.warning else colors.textSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (streakDays > 0) "$streakDays Day Streak" else "No Active Streak",
                        style = typography.caption.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                }

                Text(
                    text = "OFFLINE",
                    style = typography.label.copy(fontSize = 10.sp),
                    color = colors.accent
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(1.dp)
                .background(colors.divider)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // --- Navigation Items List ---
        val grouped = items.groupBy { it.section }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp)
        ) {
            grouped.forEach { (section, sectionItems) ->
                Text(
                    text = section,
                    style = typography.label.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = if (section == "FOCUS & TOOLS") colors.accent else colors.textSecondary,
                    modifier = Modifier.padding(start = 10.dp, top = 14.dp, bottom = 6.dp)
                )

                sectionItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val itemBg = if (isSelected) colors.surfaceSelected else Color.Transparent
                    val itemBorder = if (isSelected) colors.accent.copy(alpha = 0.35f) else Color.Transparent
                    val contentColor = if (isSelected) colors.accent else colors.textPrimary

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, itemBorder, RoundedCornerShape(12.dp))
                            .background(itemBg)
                            .clickable { onNavigate(item.route) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item.icon(if (isSelected) colors.accent else colors.textSecondary)

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = typography.bodyStrong.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = contentColor
                            )
                            if (item.subtitle != null) {
                                Text(
                                    text = item.subtitle,
                                    style = typography.caption.copy(fontSize = 11.sp),
                                    color = colors.textSecondary,
                                    maxLines = 1
                                )
                            }
                        }

                        if (item.badge != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.accent.copy(alpha = 0.15f))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = item.badge,
                                    style = typography.caption.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = colors.accent
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(colors.accent, CircleShape)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Drawer Footer ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(1.dp)
                .background(colors.divider)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LineIcons.Lock(size = 14.dp, tint = colors.textSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "100% Offline & Private",
                    style = typography.caption.copy(fontSize = 11.sp),
                    color = colors.textSecondary
                )
            }

            Text(
                text = "v1.0.1",
                style = typography.caption.copy(fontSize = 11.sp),
                color = colors.textSecondary
            )
        }
    }
}
