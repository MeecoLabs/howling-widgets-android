package eu.meecolabs.howlingwidgets.ui.hourly.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import eu.meecolabs.howlingwidgets.breezy.BreezyRepository
import eu.meecolabs.howlingwidgets.models.Version
import eu.meecolabs.howlingwidgets.models.WeatherState
import eu.meecolabs.howlingwidgets.models.WidgetLocation
import eu.meecolabs.howlingwidgets.ui.hourly.HourlyAppWidget
import eu.meecolabs.howlingwidgets.worker.HourlyUIUpdaterWorkerTask
import eu.meecolabs.howlingwidgets.worker.HourlyWidgetUpdateWorkerTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.breezyweather.datasharing.BreezyLocation
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

internal sealed interface UiState {
    data object Loading : UiState

    data object MissingApp : UiState

    data object MissingPermission : UiState

    data class IncompatibleVersion(
        val version: Version
    ) : UiState

    data class Success(
        val preferences: Preferences,
        val locations: List<BreezyLocation>
    ) : UiState

    data object Error : UiState
}

@KoinViewModel
class SettingsScreenViewModel(
    @InjectedParam private val glanceId: GlanceId,
    context: Context,
    private val contentProvider: BreezyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    internal val uiState = _uiState.asStateFlow()

    init {
        load(context)
    }

    private fun load(context: Context) = viewModelScope.launch {
        _uiState.value = try {
            if (!isPackageInstalled(context)) {
                UiState.MissingApp
            } else if (ContextCompat.checkSelfPermission(context, BreezyRepository.READ_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                UiState.MissingPermission
            } else {
                val version = contentProvider.getVersion(context)
                if (version == null) {
                    UiState.MissingApp
                } else if (!contentProvider.isCompatible(version)) {
                    UiState.IncompatibleVersion(version)
                } else {
                    val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
                    val locations = contentProvider.getLocations(context)
                    UiState.Success(prefs, locations)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            UiState.Error
        }
    }

    private fun isPackageInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getApplicationInfo(BreezyRepository.PACKAGE_NAME, 0).enabled
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun openFDroid(context: Context) {
        try {
            val intent =
                Intent(Intent.ACTION_VIEW, "https://f-droid.org/packages/${BreezyRepository.PACKAGE_NAME}".toUri())
            context.startActivity(intent)
        } catch (ex: Exception) {
            ex.printStackTrace()
            // TODO: show error
        }
    }

    fun requestPermission(launcher: ManagedActivityResultLauncher<String, Boolean>) {
        launcher.launch(BreezyRepository.READ_PERMISSION)
    }

    fun onPermissionResult(context: Context) {
        load(context)
    }

    fun openBreezy(context: Context) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(BreezyRepository.PACKAGE_NAME)
            context.startActivity(intent)
        } catch (ex: Exception) {
            ex.printStackTrace()
            // TODO: show error
        }
    }

    fun selectLocation(location: BreezyLocation, context: Context, dismiss: () -> Unit) = viewModelScope.launch  {
        val location = WidgetLocation(
            id = location.id,
            name = location.city,
            isCurrentPosition = location.isCurrentPosition
        )

        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[stringPreferencesKey(locationPrefKey)] = Json.encodeToString(location)
            prefs[stringPreferencesKey(weatherPrefKey)] = Json.encodeToString<WeatherState>(WeatherState.Loading)
        }
        HourlyAppWidget().update(context, glanceId)

        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
        val taskTag = HourlyWidgetUpdateWorkerTask.workTag(appWidgetId)

        val workManager = WorkManager.getInstance(context)

        val workRequest = OneTimeWorkRequestBuilder<HourlyWidgetUpdateWorkerTask>()
            .setInputData(workDataOf(HourlyWidgetUpdateWorkerTask.APP_WIDGET_ID_KEY to appWidgetId))
            .addTag(taskTag)
            .build()
        workManager.enqueue(workRequest)

        val next = Instant.now().atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.HOURS).plusMinutes(30)
        val hourlyWidgetUpdateRequest = PeriodicWorkRequestBuilder<HourlyWidgetUpdateWorkerTask>(Duration.ofMinutes(30))
            .setNextScheduleTimeOverride(next.toEpochSecond() * 1000)
            .setInputData(workDataOf(HourlyWidgetUpdateWorkerTask.APP_WIDGET_ID_KEY to appWidgetId))
            .build()
        workManager.enqueueUniquePeriodicWork(HourlyUIUpdaterWorkerTask.TASK_NAME, ExistingPeriodicWorkPolicy.UPDATE, hourlyWidgetUpdateRequest)

        dismiss()
    }
}
