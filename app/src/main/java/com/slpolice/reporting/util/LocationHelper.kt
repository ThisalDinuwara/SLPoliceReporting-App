package com.slpolice.reporting.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.roundToInt

/** A position fix, with enough context for the reporter to judge whether it is good enough. */
data class PlaceFix(
    val latitude: Double,
    val longitude: Double,
    val accuracyMetres: Int?,
    val address: String?,
    val fromCache: Boolean
) {
    /** Anything looser than this is too vague to pin an incident to a junction. */
    val isPrecise: Boolean get() = accuracyMetres != null && accuracyMetres <= 50

    val accuracyLabel: String
        get() = when {
            accuracyMetres == null -> "accuracy unknown"
            accuracyMetres <= 15 -> "accurate to about $accuracyMetres m"
            accuracyMetres <= 50 -> "accurate to about $accuracyMetres m"
            else -> "only accurate to about $accuracyMetres m"
        }
}

/** Why a location attempt failed, so the screen can say something specific. */
enum class LocationFailure {
    NO_PERMISSION,
    SERVICES_OFF,
    TIMED_OUT
}

sealed interface LocationResult {
    data class Found(val fix: PlaceFix) : LocationResult
    data class Failed(val reason: LocationFailure) : LocationResult
}

/**
 * Reads the device position so a report can be pinned to where the incident happened.
 *
 * A fresh high-accuracy fix is requested first. If the GPS cannot produce one in time — indoors,
 * under heavy cloud, in a built-up area — the last known position is used instead and clearly
 * marked as cached, rather than leaving the reporter with nothing.
 */
object LocationHelper {

    private const val FRESH_FIX_TIMEOUT_MS = 12_000L
    private const val CACHE_TOLERANCE_MS = 10 * 60 * 1000L

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** True when the user has location switched on at the system level. */
    fun servicesEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return runCatching {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }.getOrDefault(false)
    }

    @SuppressLint("MissingPermission")
    suspend fun currentPlace(context: Context): LocationResult {
        if (!hasPermission(context)) return LocationResult.Failed(LocationFailure.NO_PERMISSION)
        if (!servicesEnabled(context)) return LocationResult.Failed(LocationFailure.SERVICES_OFF)

        val client = LocationServices.getFusedLocationProviderClient(context)

        val fresh = withTimeoutOrNull(FRESH_FIX_TIMEOUT_MS) {
            suspendCancellableCoroutine<Location?> { continuation ->
                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setGranularity(Granularity.GRANULARITY_FINE)
                    .setDurationMillis(FRESH_FIX_TIMEOUT_MS)
                    .setMaxUpdateAgeMillis(0)
                    .build()
                client.getCurrentLocation(request, null)
                    .addOnSuccessListener { continuation.resume(it) }
                    .addOnFailureListener { continuation.resume(null) }
            }
        }

        if (fresh != null) return LocationResult.Found(describe(context, fresh, fromCache = false))

        // The GPS could not fix in time, so fall back to whatever the system already holds.
        val cached = suspendCancellableCoroutine<Location?> { continuation ->
            client.lastLocation
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(null) }
        } ?: return LocationResult.Failed(LocationFailure.TIMED_OUT)

        val stale = System.currentTimeMillis() - cached.time > CACHE_TOLERANCE_MS
        return LocationResult.Found(describe(context, cached, fromCache = stale))
    }

    private suspend fun describe(context: Context, location: Location, fromCache: Boolean): PlaceFix =
        PlaceFix(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMetres = if (location.hasAccuracy()) location.accuracy.roundToInt() else null,
            address = addressFor(context, location.latitude, location.longitude),
            fromCache = fromCache
        )

    /** Turns coordinates into something a person recognises, e.g. "Maliyadeva, Kurunegala". */
    suspend fun addressFor(context: Context, latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) return@withContext null
            runCatching {
                Geocoder(context, Locale.ENGLISH)
                    .getFromLocation(latitude, longitude, 1)
                    ?.firstOrNull()
                    ?.let { entry ->
                        listOfNotNull(
                            entry.subLocality ?: entry.thoroughfare ?: entry.featureName,
                            entry.locality,
                            entry.adminArea
                        ).distinct().joinToString(", ")
                    }
            }.getOrNull()?.ifBlank { null }
        }

    /** Straight-line separation in metres, used to compare a photo's own GPS with the pin. */
    fun distanceBetween(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double
    ): Float {
        val output = FloatArray(1)
        Location.distanceBetween(fromLat, fromLng, toLat, toLng, output)
        return output[0]
    }

    fun formatCoordinates(latitude: Double, longitude: Double): String =
        "%.5f, %.5f".format(latitude, longitude)
}
