package eu.meecolabs.howlingwidgets.ui.hourly.actions

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import eu.meecolabs.howlingwidgets.models.HourlyDisplaySettings
import eu.meecolabs.howlingwidgets.ui.hourly.HourlyAppWidget
import eu.meecolabs.howlingwidgets.ui.hourly.settings.displaySettingsPrefKey
import kotlinx.serialization.json.Json

class TogglePrecipitationDisplayAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val current: HourlyDisplaySettings = prefs[stringPreferencesKey(displaySettingsPrefKey)]?.let {
                Json.Default.decodeFromString(it)
            } ?: HourlyDisplaySettings()
            prefs[stringPreferencesKey(displaySettingsPrefKey)] =
                Json.Default.encodeToString(current.copy(showPrecipitationAmount = !current.showPrecipitationAmount))
        }

        HourlyAppWidget().update(context, glanceId)
    }
}