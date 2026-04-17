package eu.meecolabs.howlingwidgets.di

import android.content.Context
import eu.meecolabs.appupdates.AppUpdateRepository
import eu.meecolabs.appupdates.models.Repo
import eu.meecolabs.howlingwidgets.BuildConfig
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("eu.meecolabs.howlingwidgets.breezy")
class AppModule {
    @Single
    fun appUpdates(
        context: Context
    ): AppUpdateRepository =
        AppUpdateRepository(
            repoDir = context.cacheDir,
            repo = Repo(
                url = BuildConfig.APP_UPDATES_REPO_URL,
                fingerprint = BuildConfig.APP_UPDATES_REPO_FINGERPRINT
            ),
            context = context,
            isDebug = BuildConfig.DEBUG
        )
}
