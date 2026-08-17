package com.slpolice.reporting.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "users",
    indices = [Index(value = ["nic"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val nic: String,
    val phone: String,
    val email: String,
    val address: String,
    val passwordHash: String,
    val salt: String,
    val role: String,
    val badgeNumber: String? = null,
    val station: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reports",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["reporterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reporterId"), Index(value = ["referenceNo"], unique = true), Index("status")]
)
data class ReportEntity(
    @PrimaryKey val id: String,
    val referenceNo: String,
    val reporterId: Long,
    val category: String,
    val title: String,
    val description: String,
    val locationName: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val vehicleNumber: String? = null,
    val incidentAt: Long,
    val anonymous: Boolean = false,
    val status: String,
    val priority: String,
    val officerNote: String? = null,
    val handledBy: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "evidence",
    foreignKeys = [
        ForeignKey(
            entity = ReportEntity::class,
            parentColumns = ["id"],
            childColumns = ["reportId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reportId")]
)
data class EvidenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: String,
    val filePath: String,
    val type: String,
    val sizeBytes: Long,
    /** SHA-256 digest captured at upload time; proves the file was never altered afterwards. */
    val integrityHash: String,
    val capturedAt: Long = System.currentTimeMillis(),
    /** Capture time read from the file's own metadata. Null when the file carries no stamp. */
    val sourceCapturedAt: Long? = null,
    /** Coordinates read from the file's own metadata, when the camera recorded them. */
    val capturedLatitude: Double? = null,
    val capturedLongitude: Double? = null
)

@Entity(tableName = "audit_logs", indices = [Index("targetRef")])
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actorId: Long?,
    val actorName: String,
    val action: String,
    val targetRef: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

/** A report together with every media file attached to it. */
data class ReportWithEvidence(
    @Embedded val report: ReportEntity,
    @Relation(parentColumn = "id", entityColumn = "reportId")
    val evidence: List<EvidenceEntity> = emptyList()
)

/** Row shape for the officer dashboard counters. */
data class StatusCount(val status: String, val total: Int)

/**
 * A message delivered to a citizen's inbox. The department sends one automatically the moment a
 * report is accepted, and again whenever an officer moves the case or writes to the reporter.
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipientId")]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipientId: Long,
    val reportId: String? = null,
    val referenceNo: String? = null,
    val title: String,
    val body: String,
    val senderName: String,
    val kind: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/** Row shape for the category breakdown on the admin panel. */
data class CategoryCount(val category: String, val total: Int)
