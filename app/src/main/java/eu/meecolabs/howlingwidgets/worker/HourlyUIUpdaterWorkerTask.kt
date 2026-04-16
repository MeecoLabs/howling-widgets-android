package eu.meecolabs.howlingwidgets.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import eu.meecolabs.howlingwidgets.ui.hourly.HourlyAppWidget

class HourlyUIUpdaterWorkerTask(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    companion object {
        const val TASK_NAME = "hourly-ui-updater"
    }

    override suspend fun doWork(): Result {
        return try {
            HourlyAppWidget().updateAll(applicationContext)
            Result.success()
        } catch (ex: Exception) {
            ex.printStackTrace()
            Result.failure()
        }
    }
}