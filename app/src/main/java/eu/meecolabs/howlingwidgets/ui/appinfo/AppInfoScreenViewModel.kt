package eu.meecolabs.howlingwidgets.ui.appinfo

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.meecolabs.appupdates.AppUpdateRepository
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class AppInfoScreenViewModel(
    private val appUpdateRepository: AppUpdateRepository
) : ViewModel() {
    val appUpdate = appUpdateRepository.state

    fun checkForUpdates() = viewModelScope.launch {
        appUpdateRepository.checkForUpdates()
    }

    fun openFDroid(context: Context) {
        try {
            context.startActivity(appUpdateRepository.fDroidPackageDetailsIntent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
