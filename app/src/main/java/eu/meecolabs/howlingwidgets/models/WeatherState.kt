package eu.meecolabs.howlingwidgets.models

import kotlinx.serialization.Serializable
import org.breezyweather.datasharing.json.BreezyWeather

@Serializable
sealed interface WeatherState {
    @Serializable
    data object Loading : WeatherState

    @Serializable
    data object Error : WeatherState

    @Serializable
    data class Success(
        val weather: BreezyWeather
    ) : WeatherState
}
