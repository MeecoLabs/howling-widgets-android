package eu.meecolabs.howlingwidgets.models

import kotlinx.serialization.Serializable

@Serializable
data class WidgetLocation(
    val id: String,
    val name: String,
    val isCurrentPosition: Boolean
)
