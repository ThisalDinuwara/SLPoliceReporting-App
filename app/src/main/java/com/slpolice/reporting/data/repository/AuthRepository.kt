package com.slpolice.reporting.data.repository

import com.slpolice.reporting.data.Outcome
import com.slpolice.reporting.data.UserRole
import com.slpolice.reporting.data.local.AuditDao
import com.slpolice.reporting.data.local.AuditLogEntity
import com.slpolice.reporting.data.local.UserDao
import com.slpolice.reporting.data.local.UserEntity
import com.slpolice.reporting.data.remote.CloudSync
import com.slpolice.reporting.util.Security
import com.slpolice.reporting.util.Validators
import kotlinx.coroutines.flow.Flow

/**
 * Registration, sign-in and account records.
 *
 * A citizen account only exists once a valid NIC number has been supplied, and one NIC can hold
 * exactly one account. That single rule is what keeps anonymous spam out of the evidence queue.
 */
class AuthRepository(
    private val userDao: UserDao,
    private val auditDao: AuditDao
) {

    suspend fun register(
        fullName: String,
        nic: String,
        phone: String,
        email: String,
        address: String,
        password: String
    ): Outcome<Long> {
        val cleanNic = Validators.normaliseNic(nic)

        Validators.nameError(fullName)?.let { return Outcome.Failure(it) }
        Validators.nicError(cleanNic)?.let { return Outcome.Failure(it) }
        Validators.phoneError(phone)?.let { return Outcome.Failure(it) }
        Validators.emailError(email)?.let { return Outcome.Failure(it) }
        Validators.passwordError(password)?.let { return Outcome.Failure(it) }
        if (address.trim().length < 5) return Outcome.Failure("Enter your residential address")
        if (!Validators.isAdult(cleanNic)) {
            return Outcome.Failure("Reporters must be 18 or older to file evidence")
        }
        if (userDao.findByNic(cleanNic) != null) {
            return Outcome.Failure("An account already exists for this NIC number")
        }

        val salt = Security.newSalt()
        val id = userDao.insert(
            UserEntity(
                fullName = fullName.trim(),
                nic = cleanNic,
                phone = Validators.normalisePhone(phone),
                email = email.trim().lowercase(),
                address = address.trim(),
                passwordHash = Security.hashPassword(password, salt),
                salt = salt,
                role = UserRole.CITIZEN.name
            )
        )
        writeLog(id, fullName.trim(), "ACCOUNT_CREATED", cleanNic, "Citizen account verified by NIC")
        CloudSync.pushUser(
            nic = cleanNic,
            fullName = fullName.trim(),
            phone = Validators.normalisePhone(phone),
            email = email.trim().lowercase(),
            address = address.trim(),
            role = UserRole.CITIZEN.name
        )
        return Outcome.Success(id)
    }

    /**
     * Account provisioning from the admin panel. This is the only route to an officer account —
     * the public registration form can create citizens and nothing else.
     */
    suspend fun createAccount(
        actorName: String,
        fullName: String,
        nic: String,
        phone: String,
        email: String,
        address: String,
        password: String,
        role: UserRole,
        badgeNumber: String? = null,
        station: String? = null
    ): Outcome<Long> {
        val cleanNic = Validators.normaliseNic(nic)

        Validators.nameError(fullName)?.let { return Outcome.Failure(it) }
        Validators.nicError(cleanNic)?.let { return Outcome.Failure(it) }
        Validators.phoneError(phone)?.let { return Outcome.Failure(it) }
        Validators.emailError(email)?.let { return Outcome.Failure(it) }
        Validators.passwordError(password)?.let { return Outcome.Failure(it) }
        if (address.trim().length < 5) return Outcome.Failure("Enter a residential or station address")
        if (!Validators.isAdult(cleanNic)) {
            return Outcome.Failure("Account holders must be 18 or older")
        }
        if (role == UserRole.OFFICER && badgeNumber.isNullOrBlank()) {
            return Outcome.Failure("An officer account needs a badge number")
        }
        if (userDao.findByNic(cleanNic) != null) {
            return Outcome.Failure("An account already exists for this NIC number")
        }

        val salt = Security.newSalt()
        val id = userDao.insert(
            UserEntity(
                fullName = fullName.trim(),
                nic = cleanNic,
                phone = Validators.normalisePhone(phone),
                email = email.trim().lowercase(),
                address = address.trim(),
                passwordHash = Security.hashPassword(password, salt),
                salt = salt,
                role = role.name,
                badgeNumber = badgeNumber?.trim()?.ifBlank { null },
                station = station?.trim()?.ifBlank { null }
            )
        )
        writeLog(
            null,
            actorName.ifBlank { "Administrator" },
            "ACCOUNT_PROVISIONED",
            cleanNic,
            "${role.name.lowercase().replaceFirstChar { it.uppercase() }} account created from the admin panel"
        )
        CloudSync.pushUser(
            nic = cleanNic,
            fullName = fullName.trim(),
            phone = Validators.normalisePhone(phone),
            email = email.trim().lowercase(),
            address = address.trim(),
            role = role.name
        )
        return Outcome.Success(id)
    }

    suspend fun signIn(nic: String, password: String): Outcome<UserEntity> {
        val cleanNic = Validators.normaliseNic(nic)
        if (cleanNic.isBlank() || password.isBlank()) {
            return Outcome.Failure("Enter your NIC number and password")
        }
        val user = userDao.findByNic(cleanNic)
            ?: return Outcome.Failure("No account matches that NIC number")

        if (!Security.verifyPassword(password, user.salt, user.passwordHash)) {
            writeLog(user.id, user.fullName, "SIGN_IN_FAILED", cleanNic, "Wrong password entered")
            return Outcome.Failure("That password is not correct")
        }
        writeLog(user.id, user.fullName, "SIGN_IN", cleanNic, "Signed in as ${user.role}")
        return Outcome.Success(user)
    }

    suspend fun changePassword(userId: Long, current: String, replacement: String): Outcome<Unit> {
        val user = userDao.findById(userId) ?: return Outcome.Failure("Account not found")
        if (!Security.verifyPassword(current, user.salt, user.passwordHash)) {
            return Outcome.Failure("Your current password is not correct")
        }
        Validators.passwordError(replacement)?.let { return Outcome.Failure(it) }
        val salt = Security.newSalt()
        userDao.update(
            user.copy(salt = salt, passwordHash = Security.hashPassword(replacement, salt))
        )
        writeLog(user.id, user.fullName, "PASSWORD_CHANGED", user.nic, "Password replaced by holder")
        return Outcome.Success(Unit)
    }

    fun observeUser(id: Long): Flow<UserEntity?> = userDao.observeById(id)

    /** Every registered reporter, newest first. Used by the admin panel. */
    fun observeCitizens(): Flow<List<UserEntity>> = userDao.observeByRole(UserRole.CITIZEN.name)

    suspend fun findUser(id: Long): UserEntity? = userDao.findById(id)

    /**
     * Officer accounts are provisioned by the department, never self-registered. One demo officer
     * is created on first launch so the admin channel can be reviewed.
     */
    suspend fun ensureOfficerAccount() {
        if (userDao.countByRole(UserRole.OFFICER.name) > 0) return
        val salt = Security.newSalt()
        userDao.insert(
            UserEntity(
                fullName = "Inspector R. Perera",
                nic = DEMO_OFFICER_NIC,
                phone = "0112421111",
                email = "cyber.unit@police.lk",
                address = "Police Headquarters, Colombo 01",
                passwordHash = Security.hashPassword(DEMO_OFFICER_PASSWORD, salt),
                salt = salt,
                role = UserRole.OFFICER.name,
                badgeNumber = "SLP-4471",
                station = "Colombo Central Division"
            )
        )
    }

    private suspend fun writeLog(
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

    companion object {
        const val DEMO_OFFICER_NIC = "198512400123"
        const val DEMO_OFFICER_PASSWORD = "Police@2026"
    }
}
