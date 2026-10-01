package com.studyoffline.app.ui.planner

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.studyoffline.app.service.PomodoroForegroundService
import com.studyoffline.app.ui.components.StudyTopBar
import com.studyoffline.app.ui.theme.StudyOfflineTheme

@Composable
fun FocusTimerScreen(
    viewModel: PlannerViewModel,
    onMenuClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = StudyOfflineTheme.colors
    val context = LocalContext.current

    // Keep screen on effect
    DisposableEffect(state.keepScreenOn, state.isPomodoroRunning) {
        val window = (context as? Activity)?.window
        if (state.keepScreenOn && state.isPomodoroRunning) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Sync Foreground Service with Pomodoro running state
    LaunchedEffect(state.isPomodoroRunning) {
        val serviceIntent = Intent(context, PomodoroForegroundService::class.java).apply {
            action = if (state.isPomodoroRunning) {
                PomodoroForegroundService.ACTION_START
            } else if (state.pomodoroSecondsRemaining == 0) {
                PomodoroForegroundService.ACTION_STOP
            } else {
                PomodoroForegroundService.ACTION_PAUSE
            }
            putExtra(PomodoroForegroundService.EXTRA_SECONDS_REMAINING, state.pomodoroSecondsRemaining)
            putExtra(PomodoroForegroundService.EXTRA_MODE_NAME, state.pomodoroMode.title)
        }
        try {
            if (state.isPomodoroRunning) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            // Safe ignore
        }
    }

    // Remove notification when timer completes
    LaunchedEffect(state.pomodoroSecondsRemaining) {
        if (state.pomodoroSecondsRemaining == 0 && !state.isPomodoroRunning) {
            val serviceIntent = Intent(context, PomodoroForegroundService::class.java).apply {
                action = PomodoroForegroundService.ACTION_STOP
            }
            try {
                context.startService(serviceIntent)
            } catch (e: Exception) {
                // Safe ignore
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        StudyTopBar(
            title = "Focus Timer",
            onMenuClick = onMenuClick
        )

        PomodoroView(
            state = state,
            onSelectMode = { viewModel.setPomodoroMode(it) },
            onToggleRunning = { viewModel.togglePomodoro() },
            onReset = {
                val stopIntent = Intent(context, PomodoroForegroundService::class.java).apply {
                    action = PomodoroForegroundService.ACTION_STOP
                }
                context.startService(stopIntent)
                viewModel.resetPomodoro()
            },
            onToggleKeepScreenOn = { viewModel.toggleKeepScreenOn() }
        )
    }
}
