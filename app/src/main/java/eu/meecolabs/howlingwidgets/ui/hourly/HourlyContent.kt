package eu.meecolabs.howlingwidgets.ui.hourly

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import eu.meecolabs.howlingwidgets.R
import eu.meecolabs.howlingwidgets.breezy.BreezyRepository
import eu.meecolabs.howlingwidgets.models.WeatherState
import eu.meecolabs.howlingwidgets.models.WidgetLocation
import eu.meecolabs.howlingwidgets.ui.hourly.settings.WidgetSettingsActivity
import eu.meecolabs.howlingwidgets.ui.hourly.settings.appWidgetIdKey
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

private const val HOURLY_ITEMS = 5

@Composable
fun HourlyContent(
    glanceId: GlanceId,
    location: WidgetLocation,
    state: WeatherState
) {
    val context = LocalContext.current
    val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

    val now = Instant.now().atZone(ZoneId.systemDefault())

    Column(
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start,
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .padding(start = 4.dp, bottom = 4.dp)
        ) {
            if (location.isCurrentPosition) {
                Image(
                    provider = ImageProvider(R.drawable.ic_current_location),
                    contentDescription = "Current position",
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.secondary),
                    modifier = GlanceModifier.size(18.dp).padding(end = 4.dp)
                )
            }

            Text(
                text = location.name.ifBlank { "Unknown" },
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onBackground
                )
            )

            Image(
                provider = ImageProvider(R.drawable.ic_edit),
                contentDescription = "Edit",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.secondary),
                modifier = GlanceModifier
                    .padding(start = 4.dp)
                    .size(18.dp)
                    .clickable(
                        actionStartActivity<WidgetSettingsActivity>(
                            actionParametersOf(appWidgetIdKey to appWidgetId)
                        )
                    )
            )


            Spacer(GlanceModifier.defaultWeight())

            if (state is WeatherState.Success) {
                state.weather.refreshTime?.let {
                    val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
                    val formatter =
                        if (now.truncatedTo(ChronoUnit.DAYS).isEqual(date.truncatedTo(ChronoUnit.DAYS))) {
                            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                        } else {
                            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        }
                    formatter.format(date)
                }?.let { time ->
                    Text(
                        text = "Updated $time",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = GlanceTheme.colors.onBackground
                        )
                    )
                }
            }
        }

        val startBreezyAction = context.packageManager.getLaunchIntentForPackage(BreezyRepository.PACKAGE_NAME)?.apply {
            action = BreezyRepository.ACTION_SHOW_DAILY_FORECAST
            putExtra(BreezyRepository.KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID, location.id)
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }?.let { intent ->
            androidx.glance.appwidget.action.actionStartActivity(intent)
        }
        val modifier = if (startBreezyAction == null) {
            GlanceModifier.fillMaxSize()
        } else {
            GlanceModifier.fillMaxSize().clickable(startBreezyAction)
        }

        when (state) {
            is WeatherState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
            }

            is WeatherState.Error -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = modifier
                ) {
                    Text(
                        text = "Failed to retrieve weather data!",
                        style = TextStyle(
                            color = GlanceTheme.colors.onBackground
                        )
                    )
                }
            }

            is WeatherState.Success -> {
                val nowMs = now.toEpochSecond() * 1000
                val hourly = state.weather.hourly
                    ?.filter { it.date > nowMs }
                    ?.sortedBy { it.date }
                    ?.take(HOURLY_ITEMS)
                if (hourly.isNullOrEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = modifier
                    ) {
                        Text(
                            text = "Sorry, weather data is missing!",
                            style = TextStyle(
                                color = GlanceTheme.colors.onBackground
                            )
                        )
                    }
                } else {
                    Row(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = modifier
                    ) {
                        hourly.forEach {
                            HourlyItem(
                                forecast = it,
                                modifier = GlanceModifier.defaultWeight()
                            )
                        }
                    }
                }
            }
        }
    }
}
