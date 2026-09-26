package com.studyoffline.app.widget

import android.content.ComponentName
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.studyoffline.app.MainActivity
import com.studyoffline.app.data.preferences.UserPreferencesRepository
import com.studyoffline.app.di.DatabaseModule
import com.studyoffline.app.domain.CountdownCalculator
import com.studyoffline.app.ui.theme.ColorContrastUtil
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

fun glanceColor(color: Color): ColorProvider = object : ColorProvider {
    override fun getColor(context: Context): Color = color
}

// --------------------------------------------------------
// Widget A: Exam Countdown Widget (PRD §11)
// --------------------------------------------------------
class ExamCountdownWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = UserPreferencesRepository(context)
        val settings = prefs.userSettingsFlow.first()
        val daysRemaining = CountdownCalculator.calculateDaysRemaining(settings.examGoalDate)

        val accentColor = ColorContrastUtil.parseHexColor(settings.accentColorHex, Color(0xFF7C9A82))
        val surfaceColor = if (settings.themeMode == "DARK") Color(0xFF252420) else Color(0xFFFFFFFF)
        val textPrimary = if (settings.themeMode == "DARK") Color(0xFFEDE9E2) else Color(0xFF2B2A28)
        val textSecondary = if (settings.themeMode == "DARK") Color(0xFFA39D91) else Color(0xFF7A756C)

        val componentName = ComponentName(context, MainActivity::class.java)

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(surfaceColor)
                    .cornerRadius(16.dp)
                    .padding(14.dp)
                    .clickable(actionStartActivity(componentName)),
                contentAlignment = Alignment.CenterStart
            ) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    Text(
                        text = if (settings.examName.isNotEmpty()) settings.examName else "Exam Countdown",
                        style = TextStyle(
                            color = glanceColor(textSecondary),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${daysRemaining ?: 0}",
                            style = TextStyle(
                                color = glanceColor(accentColor),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Text(
                            text = if (daysRemaining == 1L) "day left" else "days left",
                            style = TextStyle(
                                color = glanceColor(textPrimary),
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

class ExamCountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ExamCountdownWidget()
}

// --------------------------------------------------------
// Widget B: Today's Plan Widget (PRD §11)
// --------------------------------------------------------
val TopicIdKey = ActionParameters.Key<Long>("topic_id")
val IsCompletedKey = ActionParameters.Key<Boolean>("is_completed")

class ToggleTopicActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val topicId = parameters[TopicIdKey] ?: return
        val currentCompleted = parameters[IsCompletedKey] ?: false

        val db = DatabaseModule.provideAppDatabase(context)
        db.topicDao().updateCompletion(topicId, !currentCompleted)
        TodaysPlanWidget().update(context, glanceId)
    }
}

class TodaysPlanWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = UserPreferencesRepository(context)
        val settings = prefs.userSettingsFlow.first()

        val db = DatabaseModule.provideAppDatabase(context)
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val startOfDay = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val scheduledTopics = db.topicDao().getTopicsScheduledForDaySync(startOfDay, endOfDay)
        val incomplete = db.topicDao().getIncompleteTopicsSync(limit = 3)
        val subjects = db.subjectDao().getAllSubjectsList().associateBy { it.id }

        val displayTopics = (scheduledTopics + incomplete).distinctBy { it.id }.take(3)

        val accentColor = ColorContrastUtil.parseHexColor(settings.accentColorHex, Color(0xFF7C9A82))
        val surfaceColor = if (settings.themeMode == "DARK") Color(0xFF252420) else Color(0xFFFFFFFF)
        val textPrimary = if (settings.themeMode == "DARK") Color(0xFFEDE9E2) else Color(0xFF2B2A28)
        val textSecondary = if (settings.themeMode == "DARK") Color(0xFFA39D91) else Color(0xFF7A756C)

        val componentName = ComponentName(context, MainActivity::class.java)

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(surfaceColor)
                    .cornerRadius(16.dp)
                    .padding(14.dp)
            ) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Plan",
                            style = TextStyle(
                                color = glanceColor(textPrimary),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )
                        Text(
                            text = "Open",
                            style = TextStyle(
                                color = glanceColor(accentColor),
                                fontSize = 12.sp
                            ),
                            modifier = GlanceModifier.clickable(actionStartActivity(componentName))
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    if (displayTopics.isEmpty()) {
                        Box(
                            modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity(componentName)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nothing scheduled — tap to plan",
                                style = TextStyle(
                                    color = glanceColor(textSecondary),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    } else {
                        Column(
                            modifier = GlanceModifier.fillMaxSize(),
                            verticalAlignment = Alignment.Top
                        ) {
                            displayTopics.forEach { topic ->
                                Row(
                                    modifier = GlanceModifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Checkbox with Glance ActionCallback
                                    Text(
                                        text = if (topic.isCompleted) "[X]" else "[  ]",
                                        style = TextStyle(
                                            color = glanceColor(if (topic.isCompleted) accentColor else textSecondary),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = GlanceModifier.clickable(
                                            actionRunCallback<ToggleTopicActionCallback>(
                                                actionParametersOf(
                                                    TopicIdKey to topic.id,
                                                    IsCompletedKey to topic.isCompleted
                                                )
                                            )
                                        )
                                    )

                                    Spacer(modifier = GlanceModifier.width(8.dp))

                                    Text(
                                        text = topic.name,
                                        style = TextStyle(
                                            color = glanceColor(if (topic.isCompleted) textSecondary else textPrimary),
                                            fontSize = 13.sp
                                        ),
                                        maxLines = 1,
                                        modifier = GlanceModifier.defaultWeight()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

class TodaysPlanWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodaysPlanWidget()
}
