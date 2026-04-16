package eu.meecolabs.howlingwidgets.ui.hourly.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import eu.meecolabs.howlingwidgets.R
import eu.meecolabs.howlingwidgets.models.WidgetLocation
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data object SettingsDestination : NavKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    glanceId: GlanceId,
    onShowAppInfo: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: SettingsScreenViewModel = koinViewModel { parametersOf(glanceId) }
) {
    val context = LocalContext.current
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        viewModel.onPermissionResult(context)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is UiState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                CircularProgressIndicator()
            }
        }

        is UiState.MissingApp -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(text = "Sorry, you do not have Breezy Weather installed.")

                Button(
                    onClick = {
                        // TODO
                    }
                ) {
                    Text(text = "Install in F-Droid")
                }
            }
        }

        is UiState.MissingPermission -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(text = "You have not granted permissions to use Breezy Weather yet.")

                Button(
                    onClick = {
                        viewModel.requestPermission(requestPermissionLauncher)
                    }
                ) {
                    Text(text = "Request permission")
                }
            }
        }

        is UiState.IncompatibleVersion -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(text = "This version of Howling Widgets is not compatible with Breezy Weather ${state.version}.")
            }
        }

        is UiState.Success -> {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(text = "Hourly Widget Settings")
                        },
                        actions = {
                            IconButton(
                                onClick = onShowAppInfo
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_info),
                                    contentDescription = "Show app info"
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                LazyColumn(Modifier
                    .fillMaxSize()
                    .padding(innerPadding)) {
                    stickyHeader {
                        Text(
                            text = "Choose which location to show on this widget:",
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.background
                                )
                                .padding(8.dp)
                        )
                    }

                    if (state.locations.isEmpty()) {
                        item {
                            Column {
                                Text(text = "You have not set up any locations in Breezy Weather yet.")

                                Button(
                                    onClick = {
                                        // TODO
                                    }
                                ) {
                                    Text(text = "Open Breezy")
                                }
                            }
                        }
                    } else {
                        val widgetLocation: WidgetLocation? = state.preferences[stringPreferencesKey(locationPrefKey)]?.let { Json.decodeFromString(it) }

                        items(state.locations) { location ->
                            LocationRow(
                                location = location,
                                isSelected = location.id == widgetLocation?.id,
                                onClick = { viewModel.selectLocation(location, context, onDismiss) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        is UiState.Error -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(text = "Something went wrong!")
            }
        }
    }
}
