package com.slpolice.reporting.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.slpolice.reporting.data.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionStore by preferencesDataStore(name = "session")

data class Session(val userId: Long?, val role: UserRole?) {
    val isSignedIn: Boolean get() = userId != null
}

/** Keeps the signed-in account across app restarts. */
class SessionManager(private val context: Context) {

    private val keyUserId = longPreferencesKey("user_id")
    private val keyRole = stringPreferencesKey("user_role")

    val session: Flow<Session> = context.sessionStore.data.map { prefs ->
        val id = prefs[keyUserId]
        Session(
            userId = if (id == null || id <= 0L) null else id,
            role = prefs[keyRole]?.let { UserRole.from(it) }
        )
    }

    suspend fun signIn(userId: Long, role: UserRole) {
        context.sessionStore.edit { prefs ->
            prefs[keyUserId] = userId
            prefs[keyRole] = role.name
        }
    }

    suspend fun signOut() {
        context.sessionStore.edit { it.clear() }
    }
}
