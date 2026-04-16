package eu.meecolabs.howlingwidgets.models

data class Version(
    val major: Int,
    val minor: Int
) {
    override fun toString(): String = "$major.$minor"
}
