package eu.meecolabs.howlingwidgets

import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import eu.meecolabs.howlingwidgets.breezy.BreezyRepository
import eu.meecolabs.howlingwidgets.models.WeatherState
import eu.meecolabs.howlingwidgets.ui.hourly.HourlyAppWidget
import eu.meecolabs.howlingwidgets.ui.hourly.settings.weatherPrefKey
import eu.meecolabs.howlingwidgets.worker.HourlyWidgetUpdateWorkerTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class HowlingAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HourlyAppWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == BreezyRepository.ACTION_UPDATE_NOTIFIER) {
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    updateWeather(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private suspend fun updateWeather(context: Context) = withContext(Dispatchers.IO) {
        val glanceManager = GlanceAppWidgetManager(context)
        val workRequests = glanceManager.getGlanceIds(HourlyAppWidget::class.java)
            .map { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[stringPreferencesKey(weatherPrefKey)] = Json.encodeToString<WeatherState>(WeatherState.Loading)
                }

                val appWidgetId = glanceManager.getAppWidgetId(glanceId)
                OneTimeWorkRequestBuilder<HourlyWidgetUpdateWorkerTask>()
                    .setInputData(workDataOf(HourlyWidgetUpdateWorkerTask.APP_WIDGET_ID_KEY to appWidgetId))
                    .build()
            }
        WorkManager.getInstance(context).enqueue(workRequests)
        glanceAppWidget.updateAll(context)
    }
}
