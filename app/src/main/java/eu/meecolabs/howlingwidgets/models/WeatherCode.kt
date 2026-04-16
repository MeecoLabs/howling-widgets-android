package eu.meecolabs.howlingwidgets.models

import androidx.annotation.DrawableRes
import eu.meecolabs.howlingwidgets.R

enum class WeatherCode(
    val id: String,
    @param:DrawableRes val dayIcon: Int,
    @param:DrawableRes val nightIcon: Int? = null
) {
    CLEAR("clear", R.drawable.clear_day, R.drawable.clear_night),
    PARTLY_CLOUDY("partly_cloudy", R.drawable.partly_cloudy_day, R.drawable.partly_cloudy_night),
    CLOUDY("cloudy", R.drawable.cloudy),
    RAIN("rain", R.drawable.overcast_rain),
    SNOW("snow", R.drawable.snow),
    WIND("wind", R.drawable.wind),
    FOG("fog", R.drawable.fog_day, R.drawable.fog_night),
    HAZE("haze", R.drawable.haze_day, R.drawable.haze_night),
    SLEET("sleet", R.drawable.sleet),
    HAIL("hail", R.drawable.hail),
    THUNDER("thunder", R.drawable.thunderstorms),
    THUNDERSTORM("thunderstorm", R.drawable.thunderstorms_rain);

    companion object {
        fun getInstance(value: String?): WeatherCode? = WeatherCode.entries.firstOrNull {
            it.id.equals(value, ignoreCase = true)
        }
    }
}
