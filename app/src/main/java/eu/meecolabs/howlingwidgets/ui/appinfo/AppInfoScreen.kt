package eu.meecolabs.howlingwidgets.ui.appinfo

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import eu.meecolabs.appupdates.AppUpdateRepository
import eu.meecolabs.howlingwidgets.BuildConfig
import eu.meecolabs.howlingwidgets.R
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel


@Serializable
data object AppInfoDestination : NavKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInfoScreen(
    showPinRequest: Boolean,
    onDismiss: (() -> Unit)? = null,
    viewModel: AppInfoScreenViewModel = koinViewModel()
) {
    val appUpdate by viewModel.appUpdate.collectAsStateWithLifecycle()

    val meteoconsComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.meteocons))
    val meteoconsAnimatable = rememberLottieAnimatable()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(meteoconsAnimatable) {
        meteoconsAnimatable.animate(meteoconsComposition)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    onDismiss?.let { onDismiss ->
                        IconButton(
                            onClick = onDismiss
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_back),
                                contentDescription = "Navigate back"
                            )
                        }
                    }
                },
                title = {
                    Text(text = stringResource(R.string.app_name))
                }
            )
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = {
                        when (appUpdate) {
                            is AppUpdateRepository.State.UpdatesAvailable -> {
                                viewModel.openFDroid(context)
                            }

                            is AppUpdateRepository.State.Checking -> {}

                            else -> {
                                viewModel.checkForUpdates()
                            }
                        }
                    })
                    .padding(12.dp)
            ) {
                Text(
                    text = "App Version",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${BuildConfig.VERSION_NAME}-b${BuildConfig.VERSION_CODE}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    when (val state = appUpdate) {
                        is AppUpdateRepository.State.Idle -> {
                            Text(
                                text = "Check for updates…",
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        is AppUpdateRepository.State.Checking -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(16.dp)
                                )

                                Text(
                                    text = "Checking…",
                                    fontWeight = FontWeight.Light,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        is AppUpdateRepository.State.Failure -> {
                            Text(
                                text = "Could not check for updates!",
                                fontWeight = FontWeight.Light,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        is AppUpdateRepository.State.NoUpdatesAvailable -> {
                            Text(
                                text = "You are on the latest version!",
                                fontWeight = FontWeight.Light,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        is AppUpdateRepository.State.UpdatesAvailable -> {
                            Text(
                                text = "New version ${state.update.versionName} available!",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Logo",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://meteocons.com/icons/?style=fill&icon=umbrella-wind-alt".toUri())
                        context.startActivity(intent)
                    }
                ) {
                    LottieAnimation(
                        composition = meteoconsComposition,
                        progress = { meteoconsAnimatable.progress },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(32.dp)
                            .clickable(onClick = {
                                if (meteoconsAnimatable.isPlaying) {
                                    return@clickable
                                }
                                scope.launch {
                                    meteoconsAnimatable.animate(meteoconsComposition)
                                }
                            })
                    )
                    Text(
                        text = "Meteocons",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            HorizontalDivider()

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Icons",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://github.com/Grabstertv/WeatherNowIcons".toUri())
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = "Weather Now",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            HorizontalDivider()

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Weather",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://github.com/breezy-weather/breezy-weather".toUri())
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = "Breezy",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            HorizontalDivider()

            Button(
                onClick = {
                    viewModel.requestAddWidget(context)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Add widget to home screen")
            }
        }
    }
}
