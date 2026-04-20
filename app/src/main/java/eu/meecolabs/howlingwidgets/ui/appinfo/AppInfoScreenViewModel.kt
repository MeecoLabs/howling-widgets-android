package eu.meecolabs.howlingwidgets.ui.appinfo

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.meecolabs.appupdates.AppUpdateRepository
import eu.meecolabs.howlingwidgets.HowlingAppWidgetReceiver
import eu.meecolabs.howlingwidgets.models.TemperatureUnit
import eu.meecolabs.howlingwidgets.models.WeatherCode
import eu.meecolabs.howlingwidgets.models.WeatherState
import eu.meecolabs.howlingwidgets.models.WidgetLocation
import eu.meecolabs.howlingwidgets.ui.hourly.HourlyAppWidget
import eu.meecolabs.howlingwidgets.ui.hourly.settings.WidgetSettingsActivity
import eu.meecolabs.howlingwidgets.ui.hourly.settings.locationPrefKey
import eu.meecolabs.howlingwidgets.ui.hourly.settings.weatherPrefKey
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.breezyweather.datasharing.json.BreezyHourly
import org.breezyweather.datasharing.json.BreezyPrecipitationProbability
import org.breezyweather.datasharing.json.BreezyTemperature
import org.breezyweather.datasharing.json.BreezyUnit
import org.breezyweather.datasharing.json.BreezyWeather
import org.koin.core.annotation.KoinViewModel
import java.time.Instant
import java.time.temporal.ChronoUnit

@KoinViewModel
class AppInfoScreenViewModel(
    private val appUpdateRepository: AppUpdateRepository
) : ViewModel() {
    val appUpdate = appUpdateRepository.state

    fun checkForUpdates() = viewModelScope.launch {
        appUpdateRepository.checkForUpdates()
    }

    fun openFDroid(context: Context) {
        try {
            context.startActivity(appUpdateRepository.fDroidPackageDetailsIntent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun requestAddWidget(context: Context) = viewModelScope.launch {
        val location = WidgetLocation("", "Howlington", false)
        val now = Instant.now().truncatedTo(ChronoUnit.HOURS)
        val state = WeatherState.Success(
            weather = BreezyWeather(
                refreshTime = now.toEpochMilli(),
                hourly = listOf(
                    BreezyHourly(
                        now.plus(1, ChronoUnit.HOURS).toEpochMilli(),
                        weatherCode = WeatherCode.CLEAR.id,
                        temperature = BreezyTemperature(temperature = BreezyUnit(value = 20.0, unit = TemperatureUnit.CELSIUS.id))
                    ),
                    BreezyHourly(
                        now.plus(2, ChronoUnit.HOURS).toEpochMilli(),
                        weatherCode = WeatherCode.PARTLY_CLOUDY.id,
                        temperature = BreezyTemperature(temperature = BreezyUnit(value = 18.0, unit = TemperatureUnit.CELSIUS.id))
                    ),
                    BreezyHourly(
                        now.plus(3, ChronoUnit.HOURS).toEpochMilli(),
                        weatherCode = WeatherCode.CLOUDY.id,
                        temperature = BreezyTemperature(temperature = BreezyUnit(value = 16.0, unit = TemperatureUnit.CELSIUS.id))
                    ),
                    BreezyHourly(
                        now.plus(4, ChronoUnit.HOURS).toEpochMilli(),
                        weatherCode = WeatherCode.RAIN.id,
                        temperature = BreezyTemperature(temperature = BreezyUnit(value = 14.0, unit = TemperatureUnit.CELSIUS.id)),
                        precipitationProbability = BreezyPrecipitationProbability(total = BreezyUnit(value = 50.0))
                    ),
                    BreezyHourly(
                        now.plus(5, ChronoUnit.HOURS).toEpochMilli(),
                        weatherCode = WeatherCode.THUNDERSTORM.id,
                        temperature = BreezyTemperature(temperature = BreezyUnit(value = 12.0, unit = TemperatureUnit.CELSIUS.id)),
                        precipitationProbability = BreezyPrecipitationProbability(total = BreezyUnit(value = 50.0))
                    )
                )
            )
        )

        val successIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, WidgetSettingsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
            receiver = HowlingAppWidgetReceiver::class.java,
            preview = HourlyAppWidget(),
            previewSize = DpSize(width = 368.dp, height = 75.dp),
            previewState = preferencesOf(
                stringPreferencesKey(locationPrefKey) to Json.encodeToString(location),
                stringPreferencesKey(weatherPrefKey) to Json.encodeToString<WeatherState>(state)
            ),
            successCallback = successIntent
        )
    }
}
