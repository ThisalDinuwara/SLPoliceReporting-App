package com.slpolice.reporting.ui.screens.report

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slpolice.reporting.data.EvidenceType
import com.slpolice.reporting.data.Priority
import com.slpolice.reporting.data.ReportCategory
import com.slpolice.reporting.data.ReportStatus
import com.slpolice.reporting.data.UserRole
import com.slpolice.reporting.data.local.EvidenceEntity
import com.slpolice.reporting.ui.AppViewModelProvider
import com.slpolice.reporting.ui.components.AppTextField
import com.slpolice.reporting.ui.components.EvidenceThumbnail
import com.slpolice.reporting.ui.components.Eyebrow
import com.slpolice.reporting.ui.components.LabelledValue
import com.slpolice.reporting.ui.components.MessageBanner
import com.slpolice.reporting.ui.components.PrimaryButton
import com.slpolice.reporting.ui.components.PriorityPill
import com.slpolice.reporting.ui.components.ReferenceTag
import com.slpolice.reporting.ui.components.SectionCard
import com.slpolice.reporting.ui.components.StatusPill
import com.slpolice.reporting.ui.components.priorityColor
import com.slpolice.reporting.ui.components.statusColor
import com.slpolice.reporting.ui.theme.BraidGold
import com.slpolice.reporting.ui.theme.Hairline
import com.slpolice.reporting.ui.theme.InkSoft
import com.slpolice.reporting.ui.theme.Navy
import com.slpolice.reporting.ui.theme.TagStyle
import com.slpolice.reporting.util.Formatters
import com.slpolice.reporting.util.MediaStorage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    onBack: () -> Unit,
    viewModel: ReportDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var confirmWithdraw by remember { mutableStateOf(false) }

    val entry = state.report

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Case file") },
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
        if (entry == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (state.loading) "Opening the case file\u2026" else "This report is no longer available",
                    color = InkSoft
                )
            }
            return@Scaffold
        }

        val report = entry.report
        val status = ReportStatus.from(report.status)
        val priority = Priority.from(report.priority)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Navy)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                ReferenceTag(report.referenceNo, modifier = Modifier.border(1.dp, BraidGold, RoundedCornerShape(6.dp)))
                Spacer(Modifier.height(14.dp))
                Text(report.title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Text(
                    ReportCategory.from(report.category).label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(status)
                    PriorityPill(priority)
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                message?.let {
                    MessageBanner(text = it, isError = false)
                    Spacer(Modifier.height(14.dp))
                }

                SectionCard {
                    Eyebrow("Evidence \u00b7 ${entry.evidence.size} file(s)")
                    Spacer(Modifier.height(12.dp))
                    if (entry.evidence.isEmpty()) {
                        Text("No files attached", color = InkSoft, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(entry.evidence, key = { it.id }) { file ->
                                EvidenceThumbnail(
                                    path = file.filePath,
                                    type = EvidenceType.from(file.type),
                                    onOpen = { openMedia(context, file) }
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        ChainOfCustody(entry.evidence)
                    }
                }

                Spacer(Modifier.height(14.dp))

                SectionCard {
                    Eyebrow("Report details")
                    Spacer(Modifier.height(8.dp))
                    LabelledValue("Description", report.description)
                    LabelledValue("Place", report.locationName)
                    if (report.latitude != null && report.longitude != null) {
                        LabelledValue(
                            "Coordinates",
                            "%.5f, %.5f".format(report.latitude, report.longitude)
                        )
                        TextButton(
                            onClick = { openMap(context, report.latitude, report.longitude, report.referenceNo) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Open on a map", color = Navy, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    report.vehicleNumber?.let { LabelledValue("Vehicle", it) }
                    LabelledValue("Incident time", Formatters.dateTime(report.incidentAt))
                    LabelledValue("Filed", Formatters.dateTime(report.createdAt))
                    report.officerNote?.let { LabelledValue("Officer note", it) }
                }

                if (state.viewerRole == UserRole.OFFICER) {
                    Spacer(Modifier.height(14.dp))
                    state.reporter?.let { ReporterSection(it.name, it.nic, it.phone, it.protected) }
                    Spacer(Modifier.height(14.dp))
                    OfficerActions(
                        current = status,
                        currentPriority = priority,
                        onStatus = { target, note -> viewModel.updateStatus(target, note) },
                        onPriority = viewModel::setPriority
                    )
                    Spacer(Modifier.height(14.dp))
                    MessageComposer(onSend = viewModel::sendMessage)
                } else if (status == ReportStatus.SUBMITTED) {
                    Spacer(Modifier.height(14.dp))
                    SectionCard {
                        Eyebrow("Withdraw")
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "You can withdraw this report until an officer opens it.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkSoft
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { confirmWithdraw = true }) {
                            Text("Withdraw this report", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                AuditTrail(
                    entries = state.trail.map { it.action to (it.details to it.timestamp) },
                    actors = state.trail.map { it.actorName }
                )
                Spacer(Modifier.height(30.dp))
            }
        }
    }

    if (confirmWithdraw) {
        AlertDialog(
            onDismissRequest = { confirmWithdraw = false },
            title = { Text("Withdraw this report?") },
            text = { Text("The report and its attached files are deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmWithdraw = false
                    viewModel.withdraw(onBack)
                }) {
                    Text("Withdraw", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmWithdraw = false }) { Text("Keep it") }
            }
        )
    }
}

/**
 * The integrity strip. Each attachment carries the checksum taken at upload, which is what lets
 * a court see the file was never swapped or edited after it left the reporter's phone.
 */
@Composable
private fun ChainOfCustody(files: List<EvidenceEntity>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Navy.copy(alpha = 0.05f))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = Navy, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Eyebrow("Integrity checksums", color = Navy)
        }
        files.forEach { file ->
            Spacer(Modifier.height(10.dp))
            Text(
                text = "${EvidenceType.from(file.type).name.lowercase()} \u00b7 ${Formatters.fileSize(file.sizeBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft
            )
            Text(
                text = "sha256:${Formatters.shortHash(file.integrityHash)}",
                style = TagStyle,
                color = Navy
            )
            Text(
                text = file.sourceCapturedAt?.let { "Recorded ${Formatters.dateTime(it)}" }
                    ?: "Capture date not present in this file",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft
            )
            if (file.capturedLatitude != null && file.capturedLongitude != null) {
                Text(
                    text = "Camera position %.5f, %.5f".format(
                        file.capturedLatitude,
                        file.capturedLongitude
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
            }
        }
    }
}

@Composable
private fun ReporterSection(name: String, nic: String, phone: String, protected: Boolean) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow("Reporter")
            if (protected) {
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.Shield, contentDescription = null, tint = BraidGold, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Protected", style = MaterialTheme.typography.bodySmall, color = BraidGold)
            }
        }
        Spacer(Modifier.height(8.dp))
        LabelledValue("Name", name)
        LabelledValue("NIC", nic)
        LabelledValue("Contact", phone)
        if (protected) {
            Spacer(Modifier.height(6.dp))
            Text(
                "This reporter asked for witness protection. Identifying details stay masked in the case view and are released only on a written request from the division head.",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft
            )
        }
    }
}

@Composable
private fun OfficerActions(
    current: ReportStatus,
    currentPriority: Priority,
    onStatus: (ReportStatus, String) -> Unit,
    onPriority: (Priority) -> Unit
) {
    var target by rememberSaveable { mutableStateOf(current.name) }
    var note by rememberSaveable { mutableStateOf("") }

    SectionCard {
        Eyebrow("Officer actions")
        Spacer(Modifier.height(12.dp))
        Text("Move this case to", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ReportStatus.entries.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { status ->
                        SelectablePill(
                            label = status.label,
                            selected = target == status.name,
                            color = statusColor(status),
                            onClick = { target = status.name },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        AppTextField(
            value = note,
            onValueChange = { note = it },
            label = "Note for the reporter",
            singleLine = false,
            minLines = 2,
            helper = "Required when a report is rejected"
        )
        Spacer(Modifier.height(14.dp))
        PrimaryButton(
            text = "Save case update",
            enabled = target != current.name || note.isNotBlank(),
            onClick = { onStatus(ReportStatus.from(target), note) }
        )

        Spacer(Modifier.height(18.dp))
        Text("Priority", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Priority.entries.forEach { priority ->
                SelectablePill(
                    label = priority.label,
                    selected = priority == currentPriority,
                    color = priorityColor(priority),
                    onClick = { onPriority(priority) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Free-text reply to the reporter. Status changes already notify them automatically, so this is
 * for anything extra an officer needs to say.
 */
@Composable
private fun MessageComposer(onSend: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }

    SectionCard {
        Eyebrow("Write to the reporter")
        Spacer(Modifier.height(6.dp))
        Text(
            "This lands in the reporter's inbox. They are already notified automatically whenever the status changes.",
            style = MaterialTheme.typography.bodySmall,
            color = InkSoft
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = text,
            onValueChange = { text = it },
            label = "Message",
            placeholder = "Thank you for the clear footage. The vehicle has been identified.",
            singleLine = false,
            minLines = 3
        )
        Spacer(Modifier.height(12.dp))
        PrimaryButton(
            text = "Send message",
            enabled = text.trim().length >= 5,
            onClick = {
                onSend(text)
                text = ""
            }
        )
    }
}

@Composable
private fun SelectablePill(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) color.copy(alpha = 0.14f) else Color.Transparent)
            .border(1.dp, if (selected) color else Hairline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) color else InkSoft
        )
    }
}

@Composable
private fun AuditTrail(
    entries: List<Pair<String, Pair<String, Long>>>,
    actors: List<String>
) {
    SectionCard {
        Eyebrow("Audit trail")
        Spacer(Modifier.height(4.dp))
        Text(
            "Every action on this case is recorded and cannot be edited.",
            style = MaterialTheme.typography.bodySmall,
            color = InkSoft
        )
        Spacer(Modifier.height(14.dp))
        entries.forEachIndexed { index, entry ->
            val (action, payload) = entry
            val (details, timestamp) = payload
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(if (index == 0) BraidGold else Hairline)
                    )
                    if (index != entries.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(46.dp)
                                .background(Hairline)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.padding(bottom = 14.dp)) {
                    Text(
                        action.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(details, style = MaterialTheme.typography.bodySmall, color = InkSoft)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${actors.getOrElse(index) { "System" }} \u00b7 ${Formatters.dateTime(timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSoft
                    )
                }
            }
        }
    }
}

private fun openMap(context: Context, latitude: Double, longitude: Double, label: String) {
    val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
}

private fun openMedia(context: Context, file: EvidenceEntity) {
    val target = File(file.filePath)
    if (!target.exists()) return
    val uri = MediaStorage.uriFor(context, target)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, if (EvidenceType.from(file.type) == EvidenceType.VIDEO) "video/*" else "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(intent) }
}
