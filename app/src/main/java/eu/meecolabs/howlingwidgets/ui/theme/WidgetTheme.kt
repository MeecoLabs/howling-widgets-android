package eu.meecolabs.howlingwidgets.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.glance.GlanceComposable
import androidx.glance.GlanceTheme
import androidx.glance.material3.ColorProviders

@Composable
fun WidgetTheme(content: @GlanceComposable @Composable () -> Unit) {
    val lightScheme = lightColorScheme()
    val darkScheme = darkColorScheme()
    GlanceTheme(
        colors = ColorProviders(lightScheme, darkScheme),
        content = content
    )
}
