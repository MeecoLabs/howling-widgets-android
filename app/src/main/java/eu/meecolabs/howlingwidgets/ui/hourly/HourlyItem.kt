package eu.meecolabs.howlingwidgets.ui.hourly

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import eu.meecolabs.howlingwidgets.R
import eu.meecolabs.howlingwidgets.models.HourlyDisplaySettings
import eu.meecolabs.howlingwidgets.models.TemperatureUnit
import eu.meecolabs.howlingwidgets.models.WeatherCode
import org.breezyweather.datasharing.json.BreezyHourly
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HourlyItem(
    forecast: BreezyHourly,
    displaySettings: HourlyDisplaySettings?,
    onChangePrecipitationDisplay: Action,
    modifier: GlanceModifier = GlanceModifier
) {
    val date = Instant.ofEpochMilli(forecast.date).atZone(ZoneId.systemDefault())
    val time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(date)

    val weatherCode = WeatherCode.getInstance(forecast.weatherCode)
    val weatherIcon = if (!forecast.isDaylight) {
        weatherCode?.nightIcon
    } else {
        null
    }
        ?: weatherCode?.dayIcon
        ?: R.drawable.not_available

    val temperatureInfo = forecast.temperature?.temperature
    val temperature = if (temperatureInfo?.value != null && temperatureInfo.unit != null) {
        val unit = TemperatureUnit.getInstance(temperatureInfo.unit)?.unit
            ?: "?"
        "%.0f%s".format(temperatureInfo.value, unit)
    } else {
        "???"
    }

    val precipitationInfo = if (displaySettings?.showPrecipitationAmount != true) {
        forecast.precipitationProbability?.total?.value
            ?.takeUnless { it < 1 }
            ?.let { "%.0f%%".format(it) }
    } else {
        forecast.precipitation?.total?.value
            ?.takeUnless { it < 0.1 }
            ?.let { "%.0f".format(it) }
    }

    Column(
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = time,
            style = TextStyle(
                fontSize = 10.sp,
                color = GlanceTheme.colors.onBackground
            )
        )
        Image(
            provider = ImageProvider(weatherIcon),
            contentDescription = forecast.weatherCode ?: "Unknown",
            contentScale = ContentScale.Fit,
            modifier = GlanceModifier
                .padding(vertical = 4.dp)
                .defaultWeight()
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = GlanceModifier
                .padding(bottom = 2.dp)
                .clickable(onChangePrecipitationDisplay)
        ) {
            Text(
                text = temperature,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onBackground
                )
            )

            if (precipitationInfo != null) {
                Text(
                    text = precipitationInfo,
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = GlanceTheme.colors.primary
                    ),
                    modifier = GlanceModifier
                        .padding(start = 4.dp)
                )
            }
        }
    }
}
