package com.slpolice.reporting.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE nic = :nic LIMIT 1")
    suspend fun findByNic(nic: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<UserEntity?>

    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    suspend fun countByRole(role: String): Int

    @Query("SELECT * FROM users WHERE role = :role ORDER BY createdAt DESC")
    fun observeByRole(role: String): Flow<List<UserEntity>>
}

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(report: ReportEntity)

    @Update
    suspend fun update(report: ReportEntity)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun delete(id: String)

    @Transaction
    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<ReportWithEvidence?>

    @Transaction
    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ReportWithEvidence?

    @Transaction
    @Query("SELECT * FROM reports WHERE reporterId = :userId ORDER BY createdAt DESC")
    fun observeByReporter(userId: Long): Flow<List<ReportWithEvidence>>

    @Transaction
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ReportWithEvidence>>

    @Query("SELECT status, COUNT(*) AS total FROM reports GROUP BY status")
    fun observeStatusCounts(): Flow<List<StatusCount>>

    @Query("SELECT COUNT(*) FROM reports WHERE reporterId = :userId")
    fun countForReporter(userId: Long): Flow<Int>

    @Query("SELECT category, COUNT(*) AS total FROM reports GROUP BY category ORDER BY total DESC")
    fun observeCategoryCounts(): Flow<List<CategoryCount>>
}

@Dao
interface EvidenceDao {
    @Insert
    suspend fun insertAll(items: List<EvidenceEntity>)

    @Query("SELECT * FROM evidence WHERE reportId = :reportId")
    suspend fun forReport(reportId: String): List<EvidenceEntity>
}

@Dao
interface AuditDao {
    @Insert
    suspend fun insert(log: AuditLogEntity)

    @Query("SELECT * FROM audit_logs WHERE targetRef = :ref ORDER BY timestamp DESC")
    fun observeForTarget(ref: String): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun observeRecent(): Flow<List<AuditLogEntity>>
}

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE recipientId = :userId ORDER BY createdAt DESC")
    fun observeForUser(userId: Long): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE recipientId = :userId AND isRead = 0")
    fun unreadCount(userId: Long): Flow<Int>

    @Query("UPDATE messages SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("UPDATE messages SET isRead = 1 WHERE recipientId = :userId")
    suspend fun markAllRead(userId: Long)

    @Query("SELECT COUNT(*) FROM messages")
    fun totalSent(): Flow<Int>
}
