package eu.meecolabs.howlingwidgets.breezy

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import eu.meecolabs.howlingwidgets.models.Version
import eu.meecolabs.howlingwidgets.models.WeatherFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.breezyweather.datasharing.BreezyLocation
import org.breezyweather.datasharing.provider.ProviderUri
import org.breezyweather.datasharing.provider.ProviderVersion
import org.koin.core.annotation.Single

@Single
class BreezyRepository {
    companion object {
        private val TAG = BreezyRepository::class.simpleName

        const val PACKAGE_NAME = "org.breezyweather"

        const val ACTION_UPDATE_NOTIFIER = "$PACKAGE_NAME.ACTION_UPDATE_NOTIFIER"

        const val ACTION_MAIN = "$PACKAGE_NAME.ACTION_MAIN"
        const val KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID = "MAIN_ACTIVITY_LOCATION_FORMATTED_ID"

        const val READ_PERMISSION = "$PACKAGE_NAME.READ_PROVIDER"
        private const val AUTHORITY = "$PACKAGE_NAME.provider.weather"
        val VERSION_URI = "content://$AUTHORITY/${ProviderUri.VERSION_PATH}".toUri()
        val LOCATIONS_URI = "content://$AUTHORITY/${ProviderUri.LOCATIONS_PATH}".toUri()
        val WEATHER_URI = "content://$AUTHORITY/${ProviderUri.WEATHER_PATH}".toUri()
    }

    fun getVersion(context: Context): Version? {
        val contentResolver = context.contentResolver
        return try {
            contentResolver.query(VERSION_URI, null, null, null, null).use { cursor ->
                if (cursor == null || cursor.count == 0) {
                    Log.w(TAG, "Content provider version not found")
                    null
                } else {
                    cursor.moveToNext()
                    val major = cursor.getInt(cursor.getColumnIndexOrThrow(ProviderVersion.COLUMN_MAJOR))
                    val minor = cursor.getInt(cursor.getColumnIndexOrThrow(ProviderVersion.COLUMN_MINOR))

                    Version(major, minor)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get content provider version", e)
            null
        }
    }

    fun isCompatible(version: Version?): Boolean {
        return version?.major == 0
    }

    fun getLocations(context: Context, limit: Int? = null): List<BreezyLocation> {
        val contentResolver = context.contentResolver
        val uri = if (limit != null && limit > 0) {
            LOCATIONS_URI.buildUpon().appendQueryParameter("limit", limit.toString()).build();
        } else {
            LOCATIONS_URI
        }

        val locations = mutableListOf<BreezyLocation>()
        contentResolver
            .query(
                uri,
                null,
                null,
                null,
                null
            ).use { cursor ->
                if (cursor == null || cursor.count == 0) {
                    Log.d(TAG, "No locations found")
                    return locations
                }
                while (cursor.moveToNext()) {
                    locations.add(BreezyLocation.toBreezyLocation(cursor))
                }
            }

        return locations
    }

    suspend fun getLocationWithWeather(context: Context, locationId: String, filters: WeatherFilter = WeatherFilter.all()): BreezyLocation? = withContext(Dispatchers.IO) {
        try {
            val uriBuilder = WEATHER_URI.buildUpon()
                .appendQueryParameter("withDaily", filters.withDaily.toString())
                .appendQueryParameter("withMinutely", filters.withMinutely.toString())
                .appendQueryParameter("withAlerts", filters.withAlerts.toString())
                .appendQueryParameter("withHourly", filters.withHourly.toString())
                .appendQueryParameter("withNormals", filters.withNormals.toString())

            val contentResolver = context.contentResolver
            contentResolver.query(
                uriBuilder.build(),
                null,
                "id=$locationId",
                null,
                null
            ).use { cursor ->
                if (cursor == null || cursor.count == 0) {
                    Log.d(TAG, "No matching location found for $locationId")
                    null
                } else {
                    cursor.moveToNext()
                    BreezyLocation.toBreezyLocation(cursor)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get weather for a location", e)
            null
        }
    }
}
