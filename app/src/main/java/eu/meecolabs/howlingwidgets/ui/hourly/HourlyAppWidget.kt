package eu.meecolabs.howlingwidgets.ui.hourly

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.WorkManager
import eu.meecolabs.howlingwidgets.models.WeatherState
import eu.meecolabs.howlingwidgets.models.WidgetLocation
import eu.meecolabs.howlingwidgets.ui.hourly.settings.locationPrefKey
import eu.meecolabs.howlingwidgets.ui.hourly.settings.weatherPrefKey
import eu.meecolabs.howlingwidgets.ui.invalid.InvalidContent
import eu.meecolabs.howlingwidgets.ui.theme.WidgetTheme
import eu.meecolabs.howlingwidgets.worker.HourlyUIUpdaterWorkerTask
import eu.meecolabs.howlingwidgets.worker.HourlyWidgetUpdateWorkerTask
import kotlinx.serialization.json.Json

class HourlyAppWidget : GlanceAppWidget() {
    override var stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val location: WidgetLocation? = prefs[stringPreferencesKey(locationPrefKey)]?.let { Json.decodeFromString(it) }
            val state: WeatherState? = prefs[stringPreferencesKey(weatherPrefKey)]?.let { Json.decodeFromString(it) }

            WidgetTheme {
                if (location == null || state == null) {
                    InvalidContent(glanceId = id)
                } else {
                    HourlyContent(
                        glanceId = id,
                        location = location,
                        state = state
                    )
                }
            }
        }
    }

    override fun onCompositionError(context: Context, glanceId: GlanceId, appWidgetId: Int, throwable: Throwable) {
        super.onCompositionError(context, glanceId, appWidgetId, throwable)

        println("onCompositionError $appWidgetId = $throwable")
    }

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        val glanceManager = GlanceAppWidgetManager(context)
        val workManager = WorkManager.getInstance(context)

        val appWidgetId = glanceManager.getAppWidgetId(glanceId)
        workManager.cancelAllWorkByTag(HourlyWidgetUpdateWorkerTask.workTag(appWidgetId))

        if (glanceManager.getGlanceIds(HourlyAppWidget::class.java).size - 1 <= 0) {
            workManager.cancelUniqueWork(HourlyUIUpdaterWorkerTask.TASK_NAME)
        }

        super.onDelete(context, glanceId)
    }
}
