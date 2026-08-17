package com.slpolice.reporting.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.slpolice.reporting.SLPoliceApp
import com.slpolice.reporting.ui.screens.admin.AdminPanelViewModel
import com.slpolice.reporting.ui.screens.admin.AdminViewModel
import com.slpolice.reporting.ui.screens.admin.CreateAccountViewModel
import com.slpolice.reporting.ui.screens.auth.AuthViewModel
import com.slpolice.reporting.ui.screens.home.HomeViewModel
import com.slpolice.reporting.ui.screens.messages.MessagesViewModel
import com.slpolice.reporting.ui.screens.profile.ProfileViewModel
import com.slpolice.reporting.ui.screens.report.ReportDetailViewModel
import com.slpolice.reporting.ui.screens.report.SubmitReportViewModel

/** One factory for every screen, so the wiring stays in a single readable place. */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            AuthViewModel(app().container.authRepository, app().container.sessionManager)
        }
        initializer {
            HomeViewModel(
                app().container.authRepository,
                app().container.reportRepository,
                app().container.sessionManager
            )
        }
        initializer {
            SubmitReportViewModel(
                app(),
                app().container.authRepository,
                app().container.reportRepository,
                app().container.sessionManager
            )
        }
        initializer {
            ReportDetailViewModel(
                createSavedStateHandle(),
                app().container.authRepository,
                app().container.reportRepository,
                app().container.sessionManager
            )
        }
        initializer {
            AdminViewModel(
                app().container.authRepository,
                app().container.reportRepository,
                app().container.sessionManager
            )
        }
        initializer {
            AdminPanelViewModel(
                app().container.authRepository,
                app().container.reportRepository,
                app().container.sessionManager
            )
        }
        initializer {
            CreateAccountViewModel(
                app().container.authRepository,
                app().container.sessionManager
            )
        }
        initializer {
            MessagesViewModel(
                app().container.reportRepository,
                app().container.sessionManager
            )
        }
        initializer {
            ProfileViewModel(
                app().container.authRepository,
                app().container.reportRepository,
                app().container.sessionManager
            )
        }
    }
}

fun CreationExtras.app(): SLPoliceApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SLPoliceApp
