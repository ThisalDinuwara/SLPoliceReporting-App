package com.slpolice.reporting.ui.screens.report

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slpolice.reporting.data.EvidenceType
import com.slpolice.reporting.data.Outcome
import com.slpolice.reporting.data.ReportCategory
import com.slpolice.reporting.data.prefs.SessionManager
import com.slpolice.reporting.data.repository.AuthRepository
import com.slpolice.reporting.data.repository.PendingEvidence
import com.slpolice.reporting.data.repository.ReportRepository
import com.slpolice.reporting.util.Formatters
import com.slpolice.reporting.util.LocationFailure
import com.slpolice.reporting.util.LocationHelper
import com.slpolice.reporting.util.LocationResult
import com.slpolice.reporting.util.MediaStorage
import com.slpolice.reporting.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

data class SubmitFormState(
    val category: ReportCategory = ReportCategory.TRAFFIC_OFFENCE,
    val title: String = "",
    val description: String = "",
    val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val vehicleNumber: String = "",
    val incidentAt: Long = System.currentTimeMillis(),
    val anonymous: Boolean = false,
    val attachments: List<PendingEvidence> = emptyList(),
    val busy: Boolean = false,
    val locating: Boolean = false,
    val accuracyMetres: Int? = null,
    val fixIsCached: Boolean = false,
    val locationServicesOff: Boolean = false,
    val evidenceDistanceMetres: Int? = null,
    val error: String? = null,
    val notice: String? = null,
    val filedReference: String? = null
) {
    val incidentLabel: String get() = Formatters.dateTime(incidentAt)
    val hasCoordinates: Boolean get() = latitude != null && longitude != null

    val pinQuality: String?
        get() = when {
            !hasCoordinates -> null
            fixIsCached -> "Using your last known position, which may be out of date"
            accuracyMetres == null -> "Pinned"
            accuracyMetres <= 50 -> "Pinned to about $accuracyMetres m"
            else -> "Rough pin, about $accuracyMetres m. Move outdoors for a sharper fix."
        }

    /** Warns when the photo's own GPS sits far from the place the reporter pinned. */
    val locationMismatch: Boolean get() = (evidenceDistanceMetres ?: 0) > 2000
    val canAddMore: Boolean get() = attachments.size < MediaStorage.MAX_ATTACHMENTS
}

class SubmitReportViewModel(
    private val app: Application,
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(SubmitFormState())
    val state: StateFlow<SubmitFormState> = _state.asStateFlow()

    private var pendingCapture: File? = null

    fun setCategory(value: ReportCategory) = update { it.copy(category = value, error = null) }
    fun setTitle(value: String) = update { it.copy(title = value.take(90), error = null) }
    fun setDescription(value: String) = update { it.copy(description = value.take(1200), error = null) }
    fun setLocationName(value: String) = update { it.copy(locationName = value, error = null) }
    fun setVehicleNumber(value: String) = update { it.copy(vehicleNumber = value.take(12), error = null) }
    fun setIncidentAt(value: Long) = update { it.copy(incidentAt = value, error = null) }
    fun setAnonymous(value: Boolean) = update { it.copy(anonymous = value) }
    fun dismissMessages() = update { it.copy(error = null, notice = null) }

    /** Creates the destination file before the camera opens and returns its shareable Uri. */
    fun prepareCapture(type: EvidenceType): Uri {
        val extension = if (type == EvidenceType.VIDEO) "mp4" else "jpg"
        val file = MediaStorage.newFile(app, extension)
        pendingCapture = file
        return MediaStorage.uriFor(app, file)
    }

    fun onCaptureResult(success: Boolean, type: EvidenceType) {
        val file = pendingCapture
        pendingCapture = null
        if (!success || file == null || !file.exists() || file.length() == 0L) {
            file?.let { MediaStorage.delete(it) }
            return
        }
        addMedia(MediaStorage.describeCapture(file, type))
    }

    fun attachFromPicker(uri: Uri) {
        viewModelScope.launch {
            update { it.copy(busy = true) }
            val media = MediaStorage.importFrom(app, uri)
            update { it.copy(busy = false) }
            if (media == null) {
                update { it.copy(error = "That file could not be read. Pick another one.") }
            } else {
                addMedia(media)
            }
        }
    }

    private fun addMedia(media: com.slpolice.reporting.util.StoredMedia) {
        val current = _state.value
        if (!current.canAddMore) {
            MediaStorage.delete(media.file)
            update { it.copy(error = "A report carries up to ${MediaStorage.MAX_ATTACHMENTS} files") }
            return
        }
        if (media.tooOld) {
            MediaStorage.delete(media.file)
            update {
                it.copy(
                    error = "That file was recorded on ${Formatters.date(media.capturedAt ?: 0L)}, " +
                        "outside the ${Validators.REPORTING_WINDOW_DAYS} day reporting window. " +
                        "Only recent footage can be filed."
                )
            }
            return
        }
        val limit = MediaStorage.sizeLimitFor(media.type)
        if (media.sizeBytes > limit) {
            MediaStorage.delete(media.file)
            update {
                it.copy(
                    error = "That file is ${Formatters.fileSize(media.sizeBytes)}. " +
                        "The limit is ${Formatters.fileSize(limit)} so uploads stay quick on mobile data."
                )
            }
            return
        }
        update {
            it.copy(
                attachments = it.attachments + PendingEvidence(media),
                error = null,
                notice = if (media.captureVerified) null else
                    "That file carries no capture date, so its age could not be verified. " +
                        "The officer reviewing your case will see this."
            )
        }
        // A newly attached photo may carry its own coordinates worth checking against the pin.
        if (media.hasCaptureLocation) compareWithEvidence()
    }

    /** Offers the photo's own coordinates as the incident location. */
    fun useEvidenceLocation() {
        val stamped = _state.value.attachments.firstOrNull { it.media.hasCaptureLocation }?.media
            ?: return
        val latitude = stamped.capturedLatitude ?: return
        val longitude = stamped.capturedLongitude ?: return
        viewModelScope.launch {
            val address = LocationHelper.addressFor(app, latitude, longitude)
            update {
                it.copy(
                    latitude = latitude,
                    longitude = longitude,
                    accuracyMetres = null,
                    fixIsCached = false,
                    evidenceDistanceMetres = 0,
                    locationName = address ?: it.locationName,
                    notice = "Using the coordinates recorded inside your photo"
                )
            }
        }
    }

    fun removeAttachment(index: Int) {
        val current = _state.value.attachments
        current.getOrNull(index)?.let { MediaStorage.delete(it.media.file) }
        update { it.copy(attachments = current.filterIndexed { i, _ -> i != index }) }
    }

    fun useCurrentLocation() {
        viewModelScope.launch {
            update { it.copy(locating = true, error = null, locationServicesOff = false) }
            when (val result = LocationHelper.currentPlace(app)) {
                is LocationResult.Found -> {
                    val fix = result.fix
                    update {
                        it.copy(
                            locating = false,
                            latitude = fix.latitude,
                            longitude = fix.longitude,
                            accuracyMetres = fix.accuracyMetres,
                            fixIsCached = fix.fromCache,
                            locationName = fix.address ?: it.locationName,
                            notice = if (fix.fromCache)
                                "Attached your last known position. Step outside and try again for a sharper fix."
                            else
                                "Location attached, ${fix.accuracyLabel}"
                        )
                    }
                    compareWithEvidence()
                }
                is LocationResult.Failed -> update {
                    it.copy(
                        locating = false,
                        locationServicesOff = result.reason == LocationFailure.SERVICES_OFF,
                        error = when (result.reason) {
                            LocationFailure.NO_PERMISSION ->
                                "Location permission is needed to pin this report. Type the place instead if you prefer."
                            LocationFailure.SERVICES_OFF ->
                                "Location is switched off on this phone. Turn it on, or type the place by hand."
                            LocationFailure.TIMED_OUT ->
                                "Could not get a fix. Move somewhere with a clearer view of the sky, or type the place."
                        }
                    )
                }
            }
        }
    }

    /** Clears a pin the reporter no longer wants attached. */
    fun clearLocation() {
        update {
            it.copy(
                latitude = null,
                longitude = null,
                accuracyMetres = null,
                fixIsCached = false,
                evidenceDistanceMetres = null
            )
        }
    }

    /**
     * Compares the pin against the coordinates the camera wrote into the photos. A large gap
     * usually means old or borrowed footage, so the reporter is told before they submit.
     */
    private fun compareWithEvidence() {
        val form = _state.value
        val pinLat = form.latitude
        val pinLng = form.longitude
        if (pinLat == null || pinLng == null) return

        val stamped = form.attachments.firstOrNull { it.media.hasCaptureLocation }?.media ?: return
        val metres = LocationHelper.distanceBetween(
            pinLat,
            pinLng,
            stamped.capturedLatitude ?: return,
            stamped.capturedLongitude ?: return
        ).toInt()

        update {
            it.copy(
                evidenceDistanceMetres = metres,
                notice = if (metres > 2000)
                    "Your photo was taken about ${metres / 1000} km from the place you pinned. " +
                        "Check the location before you submit."
                else it.notice
            )
        }
    }

    fun submit() {
        val form = _state.value
        if (form.busy) return
        Validators.vehicleError(form.vehicleNumber)?.let { message ->
            update { it.copy(error = message) }
            return
        }
        viewModelScope.launch {
            update { it.copy(busy = true, error = null) }
            val session = sessionManager.session.first()
            val userId = session.userId
            if (userId == null) {
                update { it.copy(busy = false, error = "Your session expired. Sign in again.") }
                return@launch
            }
            val reporter = authRepository.findUser(userId)
            val result = reportRepository.submit(
                reporterId = userId,
                reporterName = reporter?.fullName.orEmpty(),
                category = form.category,
                title = form.title,
                description = form.description,
                locationName = form.locationName,
                latitude = form.latitude,
                longitude = form.longitude,
                vehicleNumber = form.vehicleNumber,
                incidentAt = form.incidentAt,
                anonymous = form.anonymous,
                attachments = form.attachments
            )
            when (result) {
                is Outcome.Success -> {
                    val filed = reportRepository.report(result.data)
                    val reference = filed.first()?.report?.referenceNo.orEmpty()
                    _state.value = SubmitFormState(filedReference = reference)
                }
                is Outcome.Failure -> update { it.copy(busy = false, error = result.message) }
            }
        }
    }

    fun startNewReport() {
        _state.value = SubmitFormState()
    }

    private fun update(transform: (SubmitFormState) -> SubmitFormState) {
        _state.value = transform(_state.value)
    }

    override fun onCleared() {
        super.onCleared()
        // A capture that was started but never confirmed must not linger in the vault.
        pendingCapture?.let { MediaStorage.delete(it) }
    }
}
