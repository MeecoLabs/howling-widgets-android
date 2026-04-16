package eu.meecolabs.howlingwidgets.models

enum class TemperatureUnit(
    val id: String,
    val unit: String
) {
    CELSIUS("C", "°C"),
    FAHRENHEIT("F", "°F"),
    KELVIN("K", "°K");

    companion object {
        fun getInstance(value: String?): TemperatureUnit? = TemperatureUnit.entries.firstOrNull {
            it.id.equals(value, ignoreCase = true)
        }
    }
}
