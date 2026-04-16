package eu.meecolabs.howlingwidgets.ui.appinfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import eu.meecolabs.howlingwidgets.ui.theme.AppTheme

class AppInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AppTheme {
                Surface {
                    AppInfoScreen()
                }
            }
        }
    }
}