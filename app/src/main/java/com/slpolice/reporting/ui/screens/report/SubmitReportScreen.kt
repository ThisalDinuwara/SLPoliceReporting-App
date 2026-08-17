package com.slpolice.reporting.ui.screens.report

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slpolice.reporting.data.EvidenceType
import com.slpolice.reporting.data.ReportCategory
import com.slpolice.reporting.ui.AppViewModelProvider
import com.slpolice.reporting.ui.components.AppTextField
import com.slpolice.reporting.ui.components.EvidenceThumbnail
import com.slpolice.reporting.ui.components.Eyebrow
import com.slpolice.reporting.ui.components.MessageBanner
import com.slpolice.reporting.ui.components.PrimaryButton
import com.slpolice.reporting.ui.components.ReferenceTag
import com.slpolice.reporting.ui.components.SectionCard
import com.slpolice.reporting.ui.theme.BraidGold
import com.slpolice.reporting.ui.theme.Hairline
import com.slpolice.reporting.ui.theme.InkSoft
import com.slpolice.reporting.ui.theme.Navy
import androidx.compose.foundation.layout.PaddingValues
import com.slpolice.reporting.ui.theme.StatusAction
import com.slpolice.reporting.ui.theme.StatusRejected
import com.slpolice.reporting.ui.theme.StatusReview
import com.slpolice.reporting.ui.theme.TagStyle
import com.slpolice.reporting.util.Formatters
import com.slpolice.reporting.util.MediaStorage
import com.slpolice.reporting.util.Validators
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitReportScreen(
    onBack: () -> Unit,
    onFiled: () -> Unit,
    viewModel: SubmitReportViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        viewModel.onCaptureResult(saved, EvidenceType.IMAGE)
    }
    val recordVideo = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { saved ->
        viewModel.onCaptureResult(saved, EvidenceType.VIDEO)
    }
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.attachFromPicker(it) } }

    var awaitingCamera by remember { mutableStateOf<EvidenceType?>(null) }

    fun launchCapture(type: EvidenceType) {
        val uri = viewModel.prepareCapture(type)
        if (type == EvidenceType.IMAGE) takePhoto.launch(uri) else recordVideo.launch(uri)
    }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val pending = awaitingCamera
        awaitingCamera = null
        if (granted && pending != null) launchCapture(pending)
    }

    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) viewModel.useCurrentLocation()
    }

    fun requestCapture(type: EvidenceType) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            launchCapture(type)
        } else {
            awaitingCamera = type
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    if (state.filedReference != null) {
        FiledConfirmation(
            reference = state.filedReference.orEmpty(),
            onDone = onFiled,
            onFileAnother = { viewModel.startNewReport() }
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("File a report") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            state.error?.let {
                MessageBanner(text = it, isError = true)
                Spacer(Modifier.height(14.dp))
            }
            state.notice?.let {
                MessageBanner(text = it, isError = false)
                Spacer(Modifier.height(14.dp))
            }

            SectionCard {
                Eyebrow("What happened")
                Spacer(Modifier.height(14.dp))
                CategoryPicker(
                    selected = state.category,
                    onSelect = viewModel::setCategory
                )
                Spacer(Modifier.height(14.dp))
                AppTextField(
                    value = state.title,
                    onValueChange = viewModel::setTitle,
                    label = "Short title",
                    placeholder = "Van jumped the red light at Maliyadeva junction"
                )
                Spacer(Modifier.height(14.dp))
                AppTextField(
                    value = state.description,
                    onValueChange = viewModel::setDescription,
                    label = "Describe the incident",
                    singleLine = false,
                    minLines = 4,
                    helper = "${state.description.length} of 1200 characters"
                )
                if (state.category in vehicleCategories) {
                    Spacer(Modifier.height(14.dp))
                    AppTextField(
                        value = state.vehicleNumber,
                        onValueChange = viewModel::setVehicleNumber,
                        label = "Vehicle number (optional)",
                        placeholder = "CAB-1234"
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            SectionCard {
                Eyebrow("Where and when")
                Spacer(Modifier.height(14.dp))
                AppTextField(
                    value = state.locationName,
                    onValueChange = viewModel::setLocationName,
                    label = "Place",
                    placeholder = "Junction, road or landmark"
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            locationPermission.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        shape = RoundedCornerShape(9.dp),
                        enabled = !state.locating
                    ) {
                        Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            when {
                                state.locating -> "Finding you\u2026"
                                state.hasCoordinates -> "Update pin"
                                else -> "Use my location"
                            }
                        )
                    }
                    if (state.hasCoordinates) {
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = viewModel::clearLocation) {
                            Text("Remove", style = MaterialTheme.typography.bodySmall, color = InkSoft)
                        }
                    }
                }

                if (state.locationServicesOff) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        },
                        shape = RoundedCornerShape(9.dp)
                    ) {
                        Text("Turn on location")
                    }
                }

                if (state.hasCoordinates) {
                    Spacer(Modifier.height(12.dp))
                    PinCard(
                        coordinates = "%.5f, %.5f".format(state.latitude, state.longitude),
                        quality = state.pinQuality.orEmpty(),
                        cached = state.fixIsCached,
                        onOpenMap = {
                            val uri = Uri.parse(
                                "geo:${state.latitude},${state.longitude}?q=" +
                                    "${state.latitude},${state.longitude}(Incident)"
                            )
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, uri)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                    )
                }

                if (state.attachments.any { it.media.hasCaptureLocation }) {
                    Spacer(Modifier.height(10.dp))
                    EvidenceLocationHint(
                        mismatch = state.locationMismatch,
                        distanceMetres = state.evidenceDistanceMetres,
                        onUse = viewModel::useEvidenceLocation
                    )
                }
                Spacer(Modifier.height(14.dp))
                IncidentTimeRow(
                    label = state.incidentLabel,
                    onPick = { pickDateTime(context, state.incidentAt, viewModel::setIncidentAt) }
                )
            }

            Spacer(Modifier.height(14.dp))

            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Eyebrow("Evidence")
                    Text(
                        "${state.attachments.size} of ${MediaStorage.MAX_ATTACHMENTS}",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSoft
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Photos up to ${Formatters.fileSize(MediaStorage.MAX_IMAGE_BYTES)} and clips up to ${Formatters.fileSize(MediaStorage.MAX_VIDEO_BYTES)}. Each file is sealed with a SHA-256 checksum the moment you attach it, and its capture date is read from the file to confirm the footage is recent.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CaptureButton(
                        icon = Icons.Filled.PhotoCamera,
                        label = "Photo",
                        enabled = state.canAddMore,
                        onClick = { requestCapture(EvidenceType.IMAGE) },
                        modifier = Modifier.weight(1f)
                    )
                    CaptureButton(
                        icon = Icons.Filled.Videocam,
                        label = "Video",
                        enabled = state.canAddMore,
                        onClick = { requestCapture(EvidenceType.VIDEO) },
                        modifier = Modifier.weight(1f)
                    )
                    CaptureButton(
                        icon = Icons.Filled.PhotoLibrary,
                        label = "Gallery",
                        enabled = state.canAddMore,
                        onClick = {
                            pickMedia.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (state.attachments.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        itemsIndexed(state.attachments) { index, pending ->
                            Column {
                                EvidenceThumbnail(
                                    path = pending.media.file.absolutePath,
                                    type = pending.media.type,
                                    onRemove = { viewModel.removeAttachment(index) }
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    Formatters.fileSize(pending.media.sizeBytes),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkSoft
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Protect my identity", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Your name and phone number are masked from the case view. The department can still reach you through your NIC record if the case goes to court.",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSoft
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = state.anonymous,
                        onCheckedChange = viewModel::setAnonymous,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Navy
                        )
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            PrimaryButton(
                text = "Send to the department",
                busy = state.busy,
                onClick = viewModel::submit
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Filing a knowingly false report is an offence. Everything you send is logged against your NIC.",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}

/** Shows exactly what was pinned, so the reporter can sanity-check it before submitting. */
@Composable
private fun PinCard(
    coordinates: String,
    quality: String,
    cached: Boolean,
    onOpenMap: () -> Unit
) {
    val tint = if (cached) StatusReview else StatusAction
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.08f))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(coordinates, style = TagStyle, color = tint, modifier = Modifier.weight(1f))
            TextButton(onClick = onOpenMap) {
                Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("View", style = MaterialTheme.typography.bodySmall)
            }
        }
        if (quality.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(quality, style = MaterialTheme.typography.bodySmall, color = InkSoft)
        }
    }
}

/** Offered when a photo carries its own coordinates. */
@Composable
private fun EvidenceLocationHint(
    mismatch: Boolean,
    distanceMetres: Int?,
    onUse: () -> Unit
) {
    val tint = if (mismatch) StatusRejected else Navy
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.07f))
            .padding(12.dp)
    ) {
        Eyebrow("Your photo carries its own location", color = tint)
        Spacer(Modifier.height(5.dp))
        Text(
            text = when {
                mismatch && distanceMetres != null ->
                    "It was taken about ${distanceMetres / 1000} km from the place you pinned. " +
                        "Please check which one is right before submitting."
                distanceMetres != null && distanceMetres > 0 ->
                    "It matches your pin to within ${distanceMetres} m."
                else -> "You can use those coordinates as the incident location."
            },
            style = MaterialTheme.typography.bodySmall,
            color = InkSoft
        )
        Spacer(Modifier.height(6.dp))
        TextButton(onClick = onUse, contentPadding = PaddingValues(0.dp)) {
            Text("Use the photo's location", style = MaterialTheme.typography.bodySmall, color = tint)
        }
    }
}

private val vehicleCategories = setOf(
    ReportCategory.TRAFFIC_OFFENCE,
    ReportCategory.RECKLESS_DRIVING,
    ReportCategory.ROAD_ACCIDENT
)

@Composable
private fun CategoryPicker(selected: ReportCategory, onSelect: (ReportCategory) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Column {
        Eyebrow("Category")
        Spacer(Modifier.height(6.dp))
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Hairline, RoundedCornerShape(10.dp))
                    .clickable { open = true }
                    .padding(horizontal = 14.dp, vertical = 15.dp)
            ) {
                Text(
                    selected.label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Filled.ExpandMore, contentDescription = "Choose a category", tint = InkSoft)
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                ReportCategory.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.label) },
                        onClick = {
                            onSelect(category)
                            open = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun IncidentTimeRow(label: String, onPick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Hairline, RoundedCornerShape(10.dp))
            .clickable(onClick = onPick)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Icon(Icons.Filled.Schedule, contentDescription = null, tint = Navy, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Eyebrow("When it happened")
            Spacer(Modifier.height(3.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        Text("Change", style = MaterialTheme.typography.labelMedium, color = Navy)
    }
}

@Composable
private fun CaptureButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) Navy.copy(alpha = 0.06f) else Hairline.copy(alpha = 0.4f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (enabled) Navy else InkSoft,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) Navy else InkSoft
        )
    }
}

@Composable
private fun FiledConfirmation(reference: String, onDone: () -> Unit, onFileAnother: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = BraidGold,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            "Delivered to the department",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Quote this reference number at any station to follow the case.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(22.dp))
        ReferenceTag(reference, modifier = Modifier.border(1.dp, BraidGold, RoundedCornerShape(6.dp)))
        Spacer(Modifier.height(34.dp))
        PrimaryButton(
            text = "Back to my reports",
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = onFileAnother) {
            Text("File another report", color = BraidGold, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Native date then time pickers, so the flow matches what people already know on Android. */
private fun pickDateTime(
    context: android.content.Context,
    current: Long,
    onPicked: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = current }
    DatePickerDialog(
        context,
        { _, year, month, day ->
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    val picked = Calendar.getInstance().apply {
                        set(year, month, day, hour, minute, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onPicked(picked.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        // The picker itself enforces the reporting window, so an out-of-range date cannot even
        // be chosen rather than being rejected after the fact.
        datePicker.minDate = Validators.windowOpensAt()
        datePicker.maxDate = System.currentTimeMillis()
    }.show()
}
