package com.slpolice.reporting

import android.app.Application
import com.slpolice.reporting.data.local.AppDatabase
import com.slpolice.reporting.data.prefs.SessionManager
import com.slpolice.reporting.data.remote.CloudSync
import com.slpolice.reporting.data.repository.AuthRepository
import com.slpolice.reporting.data.repository.ReportRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application entry point. Builds the database, repositories and session store once and hands
 * them to the view models, which keeps the wiring visible instead of hidden behind a framework.
 */
class SLPoliceApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        CloudSync.initialise(this)
        container = AppContainer(this)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            container.authRepository.ensureOfficerAccount()
        }
    }
}

class AppContainer(app: Application) {
    private val database = AppDatabase.get(app)

    val sessionManager = SessionManager(app)

    val authRepository = AuthRepository(
        userDao = database.userDao(),
        auditDao = database.auditDao()
    )

    val reportRepository = ReportRepository(
        reportDao = database.reportDao(),
        evidenceDao = database.evidenceDao(),
        auditDao = database.auditDao(),
        messageDao = database.messageDao()
    )
}
