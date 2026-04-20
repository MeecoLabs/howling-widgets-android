package eu.meecolabs.howlingwidgets.ui.hourly.settings

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import eu.meecolabs.howlingwidgets.ui.appinfo.AppInfoDestination
import eu.meecolabs.howlingwidgets.ui.appinfo.AppInfoScreen
import eu.meecolabs.howlingwidgets.ui.theme.AppTheme

const val locationPrefKey = "locationState"
const val weatherPrefKey = "weatherState"
const val displaySettingsPrefKey = "displaySettings"

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
            val backStack = rememberNavBackStack(SettingsDestination)

            AppTheme {
                Surface {
                    NavDisplay(
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                        backStack = backStack,
                        entryProvider = entryProvider {
                            entry<SettingsDestination> {
                                SettingsScreen(
                                    glanceId,
                                    onShowAppInfo = {
                                        backStack.add(AppInfoDestination)
                                    },
                                    onDismiss = {
                                        dismiss(appWidgetId)
                                    }
                                )
                            }

                            entry<AppInfoDestination> {
                                AppInfoScreen(
                                    showPinRequest = false,
                                    onDismiss = {
                                        backStack.removeLastOrNull()
                                    }
                                )
                            }
                        }
                    )
                }
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
