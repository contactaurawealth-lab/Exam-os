package com.studyoffline.app.ui.blocker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.studyoffline.app.MainActivity
import com.studyoffline.app.data.preferences.UserPreferencesRepository
import com.studyoffline.app.domain.blocker.AppBlockerManager
import com.studyoffline.app.ui.components.LineIcons
import com.studyoffline.app.ui.components.StudyPrimaryButton
import com.studyoffline.app.ui.components.StudySecondaryButton
import com.studyoffline.app.ui.components.StudyTextField
import com.studyoffline.app.ui.theme.StudyOfflineTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppBlockerLockActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val targetPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""

        // Prevent bypassing lock by pressing system back button — redirect to home launcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(homeIntent)
                finish()
            }
        })

        // Resolve app name
        val appName = try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(targetPackage, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            targetPackage.ifBlank { "Distracting App" }
        }

        setContent {
            val settings by preferencesRepository.userSettingsFlow.collectAsState(initial = null)

            StudyOfflineTheme(
                themeMode = settings?.themeMode ?: "SYSTEM",
                accentColorHex = settings?.accentColorHex ?: "#7C9A82"
            ) {
                LockScreenContent(
                    appName = appName,
                    packageName = targetPackage,
                    onUnlockSuccess = {
                        AppBlockerManager.unlockForFiveMinutes(targetPackage)
                        Toast.makeText(
                            this,
                            "$appName unblocked for 5 minutes",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    },
                    onExitToStudy = {
                        val mainIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(mainIntent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun LockScreenContent(
    appName: String,
    packageName: String,
    onUnlockSuccess: () -> Unit,
    onExitToStudy: () -> Unit
) {
    val colors = StudyOfflineTheme.colors
    val typography = StudyOfflineTheme.typography

    var passwordInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isPasscodeRequired = AppBlockerManager.isPasscodeSet()

    fun attemptUnlock() {
        if (!isPasscodeRequired) {
            onUnlockSuccess()
        } else if (AppBlockerManager.verifyPasscode(passwordInput)) {
            errorMessage = null
            onUnlockSuccess()
        } else {
            errorMessage = "Incorrect passcode. Try again."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lock icon badge
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceSelected),
                contentAlignment = Alignment.Center
            ) {
                LineIcons.Lock(size = 36.dp, tint = colors.accent)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Focus Mode Active",
                style = typography.heading,
                color = colors.accent,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$appName is blocked",
                style = typography.subheading,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Exam-OS has locked this app to protect your study time. Enter your passcode to unlock for 5 minutes.",
                style = typography.body,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (isPasscodeRequired) {
                StudyTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = "Unlock Passcode",
                    placeholder = "Enter your passcode",
                    errorMessage = errorMessage,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { attemptUnlock() }
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            StudyPrimaryButton(
                text = "Unlock for 5 Minutes",
                onClick = { attemptUnlock() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            StudySecondaryButton(
                text = "Back to Study",
                onClick = onExitToStudy,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
