package com.slpolice.reporting.util

import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Password hashing and file integrity helpers.
 *
 * Passwords are never stored in plain text. Each account gets a random 16-byte salt and the
 * password is stretched with PBKDF2-HMAC-SHA256 (120,000 iterations) before it touches the
 * database, so a stolen database file still does not reveal any credential.
 */
object Security {

    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256

    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun hashPassword(password: String, salt: String): String {
        val spec = PBEKeySpec(
            password.toCharArray(),
            Base64.decode(salt, Base64.NO_WRAP),
            ITERATIONS,
            KEY_LENGTH
        )
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val candidate = hashPassword(password, salt)
        // Constant-time comparison keeps the check safe from timing analysis.
        if (candidate.length != expectedHash.length) return false
        var diff = 0
        for (i in candidate.indices) diff = diff or (candidate[i].code xor expectedHash[i].code)
        return diff == 0
    }

    /** SHA-256 digest of a media file, recorded when evidence is attached. */
    fun fileDigest(file: File): String = runCatching {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { stream ->
            val buffer = ByteArray(8 * 1024)
            while (true) {
                val read = stream.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }.getOrDefault("")
}
