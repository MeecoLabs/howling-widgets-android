package eu.meecolabs.howlingwidgets.ui.invalid

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import eu.meecolabs.howlingwidgets.ui.hourly.settings.WidgetSettingsActivity
import eu.meecolabs.howlingwidgets.ui.hourly.settings.appWidgetIdKey

@Composable
fun InvalidContent(
    glanceId: GlanceId
) {
    val context = LocalContext.current
    val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

    Column(
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(8.dp)
    ) {
        Text(
            text = "This widget does not seem to be setup correctly.",
            style = TextStyle(
                color = GlanceTheme.colors.onBackground
            ),
            modifier = GlanceModifier.padding(bottom = 4.dp)
        )

        Button(
            text = "Open Settings",
            onClick = actionStartActivity<WidgetSettingsActivity>(
                actionParametersOf(appWidgetIdKey to appWidgetId)
            )
        )
    }
}
