package com.studyoffline.app.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.studyoffline.app.ui.blocker.AppBlockerViewModel
import com.studyoffline.app.ui.components.StudyTopBar
import com.studyoffline.app.ui.theme.StudyOfflineTheme

@Composable
fun AppBlockerScreen(
    viewModel: AppBlockerViewModel,
    onMenuClick: () -> Unit
) {
    val colors = StudyOfflineTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        StudyTopBar(
            title = "App Blocker",
            onMenuClick = onMenuClick
        )

        AppBlockerTab(
            viewModel = viewModel,
            modifier = Modifier.weight(1f)
        )
    }
}
