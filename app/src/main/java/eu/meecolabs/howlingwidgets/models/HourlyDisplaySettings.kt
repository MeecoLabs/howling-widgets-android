package eu.meecolabs.howlingwidgets.models

import kotlinx.serialization.Serializable

@Serializable
data class HourlyDisplaySettings(
    val showPrecipitationAmount: Boolean = false
)
