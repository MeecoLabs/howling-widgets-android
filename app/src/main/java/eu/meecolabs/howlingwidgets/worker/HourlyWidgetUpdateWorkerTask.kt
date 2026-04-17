package eu.meecolabs.howlingwidgets.worker

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import eu.meecolabs.howlingwidgets.breezy.BreezyRepository
import eu.meecolabs.howlingwidgets.models.WeatherState
import eu.meecolabs.howlingwidgets.models.WidgetLocation
import eu.meecolabs.howlingwidgets.ui.hourly.HourlyAppWidget
import eu.meecolabs.howlingwidgets.ui.hourly.settings.locationPrefKey
import eu.meecolabs.howlingwidgets.ui.hourly.settings.weatherPrefKey
import kotlinx.serialization.json.Json
import org.breezyweather.datasharing.BreezyLocation
import org.koin.android.annotation.KoinWorker

@KoinWorker
class HourlyWidgetUpdateWorkerTask(
    context: Context,
    workerParams: WorkerParameters,
    private val breezyRepository: BreezyRepository
) : CoroutineWorker(context, workerParams) {
    companion object {
        const val APP_WIDGET_ID_KEY = "appWidgetId"

        fun workTag(appWidgetId: Int): String =
            "widget-$appWidgetId"
    }

    override suspend fun doWork(): Result {
        val appWidgetId = inputData.getInt(APP_WIDGET_ID_KEY, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return Result.failure()
        }

        val glanceId = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(appWidgetId)
        val prefs = getAppWidgetState(applicationContext, PreferencesGlanceStateDefinition, glanceId)
        val location: WidgetLocation = prefs[stringPreferencesKey(locationPrefKey)]?.let { Json.decodeFromString(it) }
            ?: return Result.failure()
        return try {
            val data = breezyRepository.getLocationWithWeather(applicationContext, location.id)
            updateAppWidget(applicationContext, glanceId, location, data)
            Result.success()
        } catch (ex: Exception) {
            ex.printStackTrace()
            updateAppWidget(applicationContext, glanceId, location, null)
            Result.failure()
        }
    }

    private suspend fun updateAppWidget(context: Context, glanceId: GlanceId, location: WidgetLocation, data: BreezyLocation?) {
        val updatedLocation = if (data == null) location else WidgetLocation(
            id = data.id,
            name = data.city,
            isCurrentPosition = data.isCurrentPosition
        )
        val newState = data?.weather?.let { WeatherState.Success(it) }
            ?: WeatherState.Error

        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[stringPreferencesKey(locationPrefKey)] = Json.encodeToString(updatedLocation)
            prefs[stringPreferencesKey(weatherPrefKey)] = Json.encodeToString<WeatherState>(newState)
        }
        HourlyAppWidget().update(context, glanceId)
    }
}
