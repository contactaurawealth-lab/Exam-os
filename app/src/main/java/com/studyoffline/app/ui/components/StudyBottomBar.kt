package com.studyoffline.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.studyoffline.app.ui.theme.StudyOfflineTheme

enum class MainTab(val title: String) {
    HOME("Home"),
    SUBJECTS("Subjects"),
    PRACTICE("Practice"),
    PLANNER("Planner"),
    PROFILE("Profile")
}

@Composable
fun StudyBottomBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    val dividerColor = colors.divider

    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .background(colors.surface)
            .defaultMinSize(minHeight = 64.dp)
            .navigationBarsPadding()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MainTab.values().forEach { tab ->
            val isSelected = tab == currentTab
            val tint = if (isSelected) colors.accent else colors.textSecondary

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        onClick = { onTabSelected(tab) }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 4dp accent dot above the icon when active (Design spec §6.5)
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(colors.accent, CircleShape)
                    )
                } else {
                    Spacer(modifier = Modifier.size(4.dp))
                }

                Spacer(modifier = Modifier.height(2.dp))

                when (tab) {
                    MainTab.HOME -> LineIcons.Home(size = 22.dp, tint = tint)
                    MainTab.SUBJECTS -> LineIcons.Book(size = 22.dp, tint = tint)
                    MainTab.PRACTICE -> LineIcons.Practice(size = 22.dp, tint = tint)
                    MainTab.PLANNER -> LineIcons.Calendar(size = 22.dp, tint = tint)
                    MainTab.PROFILE -> LineIcons.User(size = 22.dp, tint = tint)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = tab.title.uppercase(),
                    style = typography.label,
                    color = tint
                )
            }
        }
    }
}
