package com.studyoffline.app.ui.blocker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyoffline.app.data.preferences.UserPreferencesRepository
import com.studyoffline.app.domain.blocker.AppBlockerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class BlockableAppItem(
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean
)

data class AppBlockerUiState(
    val isBlockerEnabled: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val passcode: String = "",
    val blockedCount: Int = 0,
    val allApps: List<BlockableAppItem> = emptyList(),
    val filteredApps: List<BlockableAppItem> = emptyList(),
    val searchQuery: String = "",
    val isLoadingApps: Boolean = true
)

@HiltViewModel
class AppBlockerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _installedApps = MutableStateFlow<List<BlockableAppItem>>(emptyList())
    private val _isLoadingApps = MutableStateFlow(true)
    private val _isAccessibilityEnabled = MutableStateFlow(false)

    val uiState: StateFlow<AppBlockerUiState> = combine(
        preferencesRepository.userSettingsFlow,
        _installedApps,
        _searchQuery,
        _isLoadingApps,
        _isAccessibilityEnabled
    ) { settings, installed, query, isLoading, a11yEnabled ->
        val blockedSet = settings.blockedPackages
        val appsWithBlockedStatus = installed.map { app ->
            app.copy(isBlocked = blockedSet.contains(app.packageName))
        }

        val filtered = if (query.isBlank()) {
            appsWithBlockedStatus
        } else {
            appsWithBlockedStatus.filter {
                it.appName.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
            }
        }

        AppBlockerUiState(
            isBlockerEnabled = settings.appBlockerEnabled,
            isAccessibilityEnabled = a11yEnabled,
            passcode = settings.appBlockerPasscode,
            blockedCount = blockedSet.size,
            allApps = appsWithBlockedStatus,
            filteredApps = filtered,
            searchQuery = query,
            isLoadingApps = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppBlockerUiState()
    )

    init {
        loadInstalledApps()
        checkAccessibilityPermission()
    }

    fun checkAccessibilityPermission() {
        _isAccessibilityEnabled.value = isAccessibilityServiceEnabled(context)
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoadingApps.value = true
            val apps = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val myPackage = context.packageName

                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }

                val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
                resolveInfos
                    .mapNotNull { it.activityInfo?.applicationInfo }
                    .distinctBy { it.packageName }
                    .filter { it.packageName != myPackage }
                    .map { appInfo ->
                        val name = pm.getApplicationLabel(appInfo).toString()
                        BlockableAppItem(
                            packageName = appInfo.packageName,
                            appName = name,
                            isBlocked = false
                        )
                    }
                    .sortedBy { it.appName.lowercase() }
            }
            _installedApps.value = apps
            _isLoadingApps.value = false
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleBlockerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAppBlockerEnabled(enabled)
        }
    }

    fun toggleAppBlocked(packageName: String) {
        viewModelScope.launch {
            preferencesRepository.toggleBlockedPackage(packageName)
        }
    }

    fun setPasscode(passcode: String) {
        viewModelScope.launch {
            preferencesRepository.setAppBlockerPasscode(passcode)
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedService = "${context.packageName}/com.studyoffline.app.service.AppBlockerAccessibilityService"
        try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            return enabledServices.contains(expectedService, ignoreCase = true)
        } catch (e: Exception) {
            return false
        }
    }
}
