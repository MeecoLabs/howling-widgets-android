package eu.meecolabs.howlingwidgets.ui.hourly.settings

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import eu.meecolabs.howlingwidgets.ui.theme.AppTheme

val appWidgetIdKey = ActionParameters.Key<Int>(AppWidgetManager.EXTRA_APPWIDGET_ID)

const val locationPrefKey = "locationState"
const val weatherPrefKey = "weatherState"

class WidgetSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val manager = GlanceAppWidgetManager(this)
        val glanceId = manager.getGlanceIdBy(intent)
        if (glanceId == null) {
            notStartedFromWidget()
            return
        }
        val appWidgetId = manager.getAppWidgetId(glanceId)

        enableEdgeToEdge()

        setContent {
            AppTheme {
                HourlySettingsScreen(
                    glanceId,
                    onDismiss = {
                        dismiss(appWidgetId)
                    }
                )
            }
        }
    }

    private fun notStartedFromWidget() {
        Toast.makeText(applicationContext, "Cannot open widget settings for unknown widget!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun dismiss(id: Int?) {
        if (id != null) {
            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            setResult(RESULT_OK, resultValue)
        } else {
            setResult(RESULT_CANCELED)
        }
        finish()
    }
}
