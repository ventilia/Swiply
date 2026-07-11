package com.swiply.app.feature.discovery

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.swiply.app.core.common.AppError
import com.swiply.app.core.common.AppResult
import com.swiply.app.core.common.IoDispatcher
import com.swiply.app.core.common.map
import com.swiply.app.core.network.ApiCaller
import com.swiply.app.core.network.api.ProfileApi
import com.swiply.app.core.network.dto.UpdateLocationRequestDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

/**
 * Получение координат через FusedLocationProviderClient и отправка на бэкенд.
 * Город определяется обратным геокодингом НА УСТРОЙСТВЕ — точные координаты
 * уходят только на наш сервер, сторонние сервисы не участвуют.
 */
@Singleton
class LocationUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileApi: ProfileApi,
    private val apiCaller: ApiCaller,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    /** Вызывается только после выдачи runtime-разрешения. Никогда не виснет дольше таймаута. */
    @SuppressLint("MissingPermission")
    suspend fun updateLocation(): AppResult<Unit> {
        val location = fetchBestLocation()
            ?: return AppResult.Failure(AppError("NO_LOCATION_FIX", "Не удалось определить местоположение"))

        val city = resolveCity(location)
        return apiCaller.call {
            profileApi.updateLocation(
                UpdateLocationRequestDto(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    city = city,
                ),
            )
        }.map { }
    }

    /**
     * Свежий фикс с ограничением по времени, fallback на последнюю известную точку.
     * На эмуляторе без заданной локации getCurrentLocation() может не вернуться никогда —
     * поэтому оба вызова обёрнуты в таймаут, чтобы не подвесить экран.
     */
    @SuppressLint("MissingPermission")
    private suspend fun fetchBestLocation(): Location? = runCatching {
        withTimeoutOrNull(8.seconds) {
            fusedClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token,
            ).await()
        } ?: withTimeoutOrNull(2.seconds) { fusedClient.lastLocation.await() }
    }.getOrNull()

    private suspend fun resolveCity(location: Location): String? = withContext(ioDispatcher) {
        runCatching {
            @Suppress("DEPRECATION") // async-вариант появился в API 33, minSdk 26 — блокирующий вызов в IO
            Geocoder(context, Locale.getDefault())
                .getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()
                ?.locality
        }.getOrNull()
    }
}
