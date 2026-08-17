package com.slpolice.reporting.data.remote

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase

/**
 * Publishes department records to Firebase Realtime Database.
 *
 * Room stays the source of truth so the app keeps working with no network at all. This layer is a
 * one-way mirror that gives the department a live web console. Password hashes and salts are never
 * uploaded, and evidence files stay in device storage — only their SHA-256 checksums travel.
 *
 * Firebase is initialised in code rather than through a google-services.json file, so the project
 * compiles and runs whether or not a Firebase account has been set up. Leave [DATABASE_URL] blank
 * to switch cloud syncing off entirely.
 */
object CloudSync {

    /** Realtime Database URL from the Firebase console. Blank disables all syncing. */
    private const val DATABASE_URL =
        "https://slpolicereportingdb-default-rtdb.asia-southeast1.firebasedatabase.app"

    /** Read from Firebase console → Project settings → General. Optional for open-rule databases. */
    private const val PROJECT_ID = "slpolicereportingdb"
    private const val APPLICATION_ID = "1:000000000000:android:0000000000000000"
    private const val API_KEY = "unused-for-realtime-database"

    private const val TAG = "CloudSync"
    private var database: FirebaseDatabase? = null

    val enabled: Boolean get() = database != null

    /** Called once from the Application class. Failure here must never stop the app starting. */
    fun initialise(context: Context) {
        if (DATABASE_URL.isBlank()) {
            Log.i(TAG, "No database URL configured, running local only")
            return
        }
        runCatching {
            val existing = FirebaseApp.getApps(context).firstOrNull()
            val app = existing ?: FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId(APPLICATION_ID)
                    .setApiKey(API_KEY)
                    .setProjectId(PROJECT_ID)
                    .setDatabaseUrl(DATABASE_URL)
                    .build()
            )
            database = FirebaseDatabase.getInstance(app, DATABASE_URL).apply {
                setPersistenceEnabled(true)
            }
            Log.i(TAG, "Cloud mirror ready")
        }.onFailure { Log.w(TAG, "Cloud mirror unavailable: ${it.message}") }
    }

    fun pushUser(
        nic: String,
        fullName: String,
        phone: String,
        email: String,
        address: String,
        role: String
    ) = safely { db ->
        db.getReference("users").child(nic).setValue(
            mapOf(
                "nic" to nic,
                "fullName" to fullName,
                "phone" to phone,
                "email" to email,
                "address" to address,
                "role" to role,
                "registeredAt" to System.currentTimeMillis()
            )
        )
    }

    fun pushReport(
        reference: String,
        division: String,
        category: String,
        title: String,
        description: String,
        location: String,
        latitude: Double?,
        longitude: Double?,
        vehicleNumber: String?,
        incidentAt: Long,
        anonymous: Boolean,
        status: String,
        priority: String,
        evidenceCount: Int,
        evidenceChecksums: List<String>,
        captureVerified: Boolean,
        locationVerified: Boolean,
        createdAt: Long
    ) = safely { db ->
        db.getReference("reports").child(reference).setValue(
            mapOf(
                "referenceNo" to reference,
                "division" to division,
                "category" to category,
                "title" to title,
                "description" to description,
                "location" to location,
                "latitude" to latitude,
                "longitude" to longitude,
                "vehicleNumber" to vehicleNumber,
                "incidentAt" to incidentAt,
                "witnessProtected" to anonymous,
                "status" to status,
                "priority" to priority,
                "evidenceCount" to evidenceCount,
                "evidenceChecksums" to evidenceChecksums,
                "captureTimeVerified" to captureVerified,
                "cameraLocationPresent" to locationVerified,
                "createdAt" to createdAt,
                "updatedAt" to createdAt
            )
        )
    }

    fun updateReportStatus(reference: String, status: String, note: String?) = safely { db ->
        val branch = db.getReference("reports").child(reference)
        branch.child("status").setValue(status)
        branch.child("updatedAt").setValue(System.currentTimeMillis())
        if (!note.isNullOrBlank()) branch.child("officerNote").setValue(note)
    }

    fun updateReportPriority(reference: String, priority: String) = safely { db ->
        db.getReference("reports").child(reference).child("priority").setValue(priority)
    }

    fun removeReport(reference: String) = safely { db ->
        db.getReference("reports").child(reference).removeValue()
    }

    fun pushAudit(actorName: String, action: String, target: String, details: String) = safely { db ->
        db.getReference("auditLogs").push().setValue(
            mapOf(
                "actor" to actorName,
                "action" to action,
                "target" to target,
                "details" to details,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }

    /** Cloud trouble must never block a citizen from filing evidence, so failures are swallowed. */
    private fun safely(block: (FirebaseDatabase) -> Unit) {
        val db = database ?: return
        runCatching { block(db) }.onFailure { Log.w(TAG, "Sync failed: ${it.message}") }
    }
}
