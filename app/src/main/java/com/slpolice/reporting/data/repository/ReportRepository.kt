package com.slpolice.reporting.data.repository

import com.slpolice.reporting.data.Outcome
import com.slpolice.reporting.data.Priority
import com.slpolice.reporting.data.ReportCategory
import com.slpolice.reporting.data.ReportStatus
import com.slpolice.reporting.data.local.AuditDao
import com.slpolice.reporting.data.local.AuditLogEntity
import com.slpolice.reporting.data.MessageKind
import com.slpolice.reporting.data.local.EvidenceDao
import com.slpolice.reporting.data.local.EvidenceEntity
import com.slpolice.reporting.data.local.MessageDao
import com.slpolice.reporting.data.local.MessageEntity
import com.slpolice.reporting.data.local.ReportDao
import com.slpolice.reporting.data.local.ReportEntity
import com.slpolice.reporting.data.local.ReportWithEvidence
import com.slpolice.reporting.data.local.StatusCount
import com.slpolice.reporting.data.remote.CloudSync
import com.slpolice.reporting.data.group
import com.slpolice.reporting.data.local.CategoryCount
import com.slpolice.reporting.util.StoredMedia
import com.slpolice.reporting.util.Validators
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

/** A single attachment as it is held in the compose screen before the report is filed. */
data class PendingEvidence(val media: StoredMedia)

class ReportRepository(
    private val reportDao: ReportDao,
    private val evidenceDao: EvidenceDao,
    private val auditDao: AuditDao,
    private val messageDao: MessageDao
) {

    fun myReports(userId: Long): Flow<List<ReportWithEvidence>> = reportDao.observeByReporter(userId)

    fun allReports(): Flow<List<ReportWithEvidence>> = reportDao.observeAll()

    fun report(id: String): Flow<ReportWithEvidence?> = reportDao.observeById(id)

    fun statusCounts(): Flow<List<StatusCount>> = reportDao.observeStatusCounts()

    fun myReportCount(userId: Long): Flow<Int> = reportDao.countForReporter(userId)

    fun trailFor(reference: String): Flow<List<AuditLogEntity>> = auditDao.observeForTarget(reference)

    fun recentActivity(): Flow<List<AuditLogEntity>> = auditDao.observeRecent()

    fun categoryCounts(): Flow<List<CategoryCount>> = reportDao.observeCategoryCounts()

    fun inbox(userId: Long): Flow<List<MessageEntity>> = messageDao.observeForUser(userId)

    fun unreadCount(userId: Long): Flow<Int> = messageDao.unreadCount(userId)

    suspend fun markMessageRead(id: Long) = messageDao.markRead(id)

    suspend fun markInboxRead(userId: Long) = messageDao.markAllRead(userId)

    suspend fun submit(
        reporterId: Long,
        reporterName: String,
        category: ReportCategory,
        title: String,
        description: String,
        locationName: String,
        latitude: Double?,
        longitude: Double?,
        vehicleNumber: String?,
        incidentAt: Long,
        anonymous: Boolean,
        attachments: List<PendingEvidence>
    ): Outcome<String> {
        if (title.trim().length < 6) return Outcome.Failure("Give the report a clear short title")
        if (description.trim().length < 20) {
            return Outcome.Failure("Describe what happened in at least 20 characters")
        }
        if (locationName.trim().length < 3) return Outcome.Failure("Enter where this happened")
        if (attachments.isEmpty()) return Outcome.Failure("Attach at least one photo or video")
        if (incidentAt > System.currentTimeMillis() + 60_000) {
            return Outcome.Failure("The incident time cannot be in the future")
        }
        // Evidence must be fresh. Old footage cannot be verified against conditions on the day,
        // and stale reports clog the queue, so the window closes after a week.
        if (!Validators.withinReportingWindow(incidentAt)) {
            return Outcome.Failure(
                "Incidents must be reported within ${Validators.REPORTING_WINDOW_DAYS} days. " +
                    "For anything older, please visit your nearest police station."
            )
        }
        attachments.firstOrNull { it.media.tooOld }?.let {
            return Outcome.Failure(
                "One of your files was recorded more than ${Validators.REPORTING_WINDOW_DAYS} days " +
                    "ago. Only recent footage can be filed here."
            )
        }

        val id = UUID.randomUUID().toString()
        val reference = newReference()
        val now = System.currentTimeMillis()

        reportDao.insert(
            ReportEntity(
                id = id,
                referenceNo = reference,
                reporterId = reporterId,
                category = category.name,
                title = title.trim(),
                description = description.trim(),
                locationName = locationName.trim(),
                latitude = latitude,
                longitude = longitude,
                vehicleNumber = vehicleNumber?.trim()?.uppercase(Locale.ROOT)?.ifBlank { null },
                incidentAt = incidentAt,
                anonymous = anonymous,
                status = ReportStatus.SUBMITTED.name,
                priority = priorityFor(category).name,
                createdAt = now,
                updatedAt = now
            )
        )

        evidenceDao.insertAll(
            attachments.map { pending ->
                EvidenceEntity(
                    reportId = id,
                    filePath = pending.media.file.absolutePath,
                    type = pending.media.type.name,
                    sizeBytes = pending.media.sizeBytes,
                    integrityHash = pending.media.integrityHash,
                    sourceCapturedAt = pending.media.capturedAt,
                    capturedLatitude = pending.media.capturedLatitude,
                    capturedLongitude = pending.media.capturedLongitude
                )
            }
        )

        CloudSync.pushReport(
            reference = reference,
            division = category.group.name,
            category = category.name,
            title = title.trim(),
            description = description.trim(),
            location = locationName.trim(),
            latitude = latitude,
            longitude = longitude,
            vehicleNumber = vehicleNumber?.trim()?.uppercase(Locale.ROOT)?.ifBlank { null },
            incidentAt = incidentAt,
            anonymous = anonymous,
            status = ReportStatus.SUBMITTED.name,
            priority = priorityFor(category).name,
            evidenceCount = attachments.size,
            evidenceChecksums = attachments.map { it.media.integrityHash },
            captureVerified = attachments.all { it.media.captureVerified },
            locationVerified = attachments.any { it.media.hasCaptureLocation },
            createdAt = now
        )

        log(
            reporterId,
            if (anonymous) "Protected reporter" else reporterName,
            "REPORT_FILED",
            reference,
            "${attachments.size} file(s) sealed and delivered to the department"
        )

        // The department acknowledges every accepted report straight away, so the reporter
        // knows their evidence arrived rather than being left wondering.
        messageDao.insert(
            MessageEntity(
                recipientId = reporterId,
                reportId = id,
                referenceNo = reference,
                title = "Thank you for your report",
                body = "Your report has been received by the Sri Lanka Police Department and " +
                    "registered under reference " + reference + ". " +
                    "Your evidence was delivered securely and is now in the review queue.\n\n" +
                    "An officer will examine the material and you will be notified here when the " +
                    "status changes. Please keep this reference number for any enquiry at a station.\n\n" +
                    "Reporting an incident helps keep our roads and communities safe. " +
                    "Do not approach or confront anyone involved. If you are in immediate danger, call 119.",
                senderName = "Sri Lanka Police Department",
                kind = MessageKind.SYSTEM.name
            )
        )
        return Outcome.Success(id)
    }

    suspend fun updateStatus(
        reportId: String,
        officerId: Long,
        officerName: String,
        status: ReportStatus,
        note: String?
    ): Outcome<Unit> {
        val existing = reportDao.findById(reportId)?.report
            ?: return Outcome.Failure("Report not found")
        if (status == ReportStatus.REJECTED && note.isNullOrBlank()) {
            return Outcome.Failure("Add a reason before rejecting a report")
        }
        reportDao.update(
            existing.copy(
                status = status.name,
                officerNote = note?.trim()?.ifBlank { null } ?: existing.officerNote,
                handledBy = officerId,
                updatedAt = System.currentTimeMillis()
            )
        )
        log(
            officerId,
            officerName,
            "STATUS_${status.name}",
            existing.referenceNo,
            note?.trim()?.ifBlank { null } ?: "Status moved to ${status.label}"
        )
        CloudSync.updateReportStatus(existing.referenceNo, status.name, note?.trim())

        messageDao.insert(
            MessageEntity(
                recipientId = existing.reporterId,
                reportId = reportId,
                referenceNo = existing.referenceNo,
                title = headlineFor(status),
                body = bodyFor(status, existing.referenceNo, note?.trim()?.ifBlank { null }),
                senderName = officerName.ifBlank { "Sri Lanka Police Department" },
                kind = MessageKind.OFFICER.name
            )
        )
        return Outcome.Success(Unit)
    }

    suspend fun setPriority(
        reportId: String,
        officerId: Long,
        officerName: String,
        priority: Priority
    ): Outcome<Unit> {
        val existing = reportDao.findById(reportId)?.report
            ?: return Outcome.Failure("Report not found")
        reportDao.update(
            existing.copy(priority = priority.name, updatedAt = System.currentTimeMillis())
        )
        CloudSync.updateReportPriority(existing.referenceNo, priority.name)
        log(officerId, officerName, "PRIORITY_SET", existing.referenceNo, "Priority set to ${priority.label}")
        return Outcome.Success(Unit)
    }

    suspend fun withdraw(reportId: String, userId: Long, userName: String): Outcome<Unit> {
        val existing = reportDao.findById(reportId)?.report
            ?: return Outcome.Failure("Report not found")
        if (existing.reporterId != userId) return Outcome.Failure("This report belongs to another account")
        if (existing.status != ReportStatus.SUBMITTED.name) {
            return Outcome.Failure("An officer has already opened this report, so it cannot be withdrawn")
        }
        reportDao.delete(reportId)
        CloudSync.removeReport(existing.referenceNo)
        log(userId, userName, "REPORT_WITHDRAWN", existing.referenceNo, "Withdrawn by the reporter")
        return Outcome.Success(Unit)
    }

    /** A free-text note an officer writes directly to the reporter. */
    suspend fun sendMessage(
        reportId: String,
        officerId: Long,
        officerName: String,
        text: String
    ): Outcome<Unit> {
        if (text.trim().length < 5) return Outcome.Failure("Write a message before sending")
        val existing = reportDao.findById(reportId)?.report
            ?: return Outcome.Failure("Report not found")

        messageDao.insert(
            MessageEntity(
                recipientId = existing.reporterId,
                reportId = reportId,
                referenceNo = existing.referenceNo,
                title = "Message about " + existing.referenceNo,
                body = text.trim(),
                senderName = officerName.ifBlank { "Sri Lanka Police Department" },
                kind = MessageKind.OFFICER.name
            )
        )
        log(officerId, officerName, "MESSAGE_SENT", existing.referenceNo, "Officer wrote to the reporter")
        return Outcome.Success(Unit)
    }

    private fun headlineFor(status: ReportStatus): String = when (status) {
        ReportStatus.SUBMITTED -> "Your report is back in the queue"
        ReportStatus.UNDER_REVIEW -> "An officer has opened your report"
        ReportStatus.ACTION_TAKEN -> "Action has been taken on your report"
        ReportStatus.REJECTED -> "Your report has been closed"
    }

    private fun bodyFor(status: ReportStatus, reference: String, note: String?): String {
        val opening = when (status) {
            ReportStatus.SUBMITTED ->
                "Report " + reference + " has been returned to the review queue."
            ReportStatus.UNDER_REVIEW ->
                "An officer is now examining the evidence you submitted under " + reference + ". " +
                    "You do not need to do anything further at this stage."
            ReportStatus.ACTION_TAKEN ->
                "Thank you for reporting this. The department has acted on the evidence you " +
                    "submitted under " + reference + ". Your contribution made this possible."
            ReportStatus.REJECTED ->
                "After review, no further action will be taken on report " + reference + "."
        }
        return if (note.isNullOrBlank()) opening else opening + "\n\nOfficer's note:\n" + note
    }

    private fun priorityFor(category: ReportCategory): Priority = when (category) {
        ReportCategory.ASSAULT, ReportCategory.ROAD_ACCIDENT -> Priority.CRITICAL
        ReportCategory.DRUG_ACTIVITY, ReportCategory.THEFT_OR_ROBBERY, ReportCategory.RECKLESS_DRIVING -> Priority.HIGH
        ReportCategory.PUBLIC_NUISANCE, ReportCategory.ENVIRONMENTAL -> Priority.LOW
        else -> Priority.NORMAL
    }

    private fun newReference(): String {
        val stamp = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).format(Date())
        val suffix = Random.nextInt(1000, 9999)
        return "SLP-$stamp-$suffix"
    }

    private suspend fun log(
        actorId: Long?,
        actorName: String,
        action: String,
        target: String,
        details: String
    ) {
        auditDao.insert(
            AuditLogEntity(
                actorId = actorId,
                actorName = actorName,
                action = action,
                targetRef = target,
                details = details
            )
        )
        CloudSync.pushAudit(actorName, action, target, details)
    }
}
