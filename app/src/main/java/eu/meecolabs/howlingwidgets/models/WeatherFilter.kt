package eu.meecolabs.howlingwidgets.models

data class WeatherFilter(
    val withDaily: Boolean = false,
    val withHourly: Boolean = false,
    val withMinutely: Boolean = false,
    val withAlerts: Boolean = false,
    val withNormals: Boolean = false,
) {
    companion object {
        fun all(): WeatherFilter = WeatherFilter(
            withDaily = true,
            withHourly = true,
            withMinutely = true,
            withAlerts = true,
            withNormals = true
        )
    }
}
