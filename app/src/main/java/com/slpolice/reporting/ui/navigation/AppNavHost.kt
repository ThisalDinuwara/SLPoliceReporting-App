package com.slpolice.reporting.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.slpolice.reporting.data.UserRole
import com.slpolice.reporting.ui.screens.admin.AdminDashboardScreen
import com.slpolice.reporting.ui.screens.admin.AdminPanelScreen
import com.slpolice.reporting.ui.screens.admin.CreateAccountScreen
import com.slpolice.reporting.ui.screens.auth.LoginScreen
import com.slpolice.reporting.ui.screens.auth.RegisterScreen
import com.slpolice.reporting.ui.screens.home.HomeScreen
import com.slpolice.reporting.ui.screens.messages.MessagesScreen
import com.slpolice.reporting.ui.screens.profile.ProfileScreen
import com.slpolice.reporting.ui.screens.report.ReportDetailScreen
import com.slpolice.reporting.ui.screens.report.SubmitReportScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val SUBMIT = "submit"
    const val ADMIN = "admin"
    const val PROFILE = "profile"
    const val MESSAGES = "messages"
    const val ADMIN_PANEL = "adminPanel"
    const val CREATE_ACCOUNT = "createAccount"
    const val REPORT_DETAIL = "report/{reportId}"

    fun reportDetail(reportId: String) = "report/$reportId"
}

@Composable
fun AppNavHost(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    fun goHomeFor(role: UserRole) {
        val target = if (role == UserRole.OFFICER) Routes.ADMIN else Routes.HOME
        navController.navigate(target) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun goToLogin() {
        navController.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onSignedIn = ::goHomeFor,
                onCreateAccount = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegistered = { goHomeFor(UserRole.CITIZEN) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNewReport = { navController.navigate(Routes.SUBMIT) },
                onOpenReport = { navController.navigate(Routes.reportDetail(it)) },
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onOpenMessages = { navController.navigate(Routes.MESSAGES) }
            )
        }

        composable(Routes.SUBMIT) {
            SubmitReportScreen(
                onBack = { navController.popBackStack() },
                onFiled = { navController.popBackStack(Routes.HOME, inclusive = false) }
            )
        }

        composable(Routes.ADMIN) {
            AdminDashboardScreen(
                onOpenReport = { navController.navigate(Routes.reportDetail(it)) },
                onOpenPanel = { navController.navigate(Routes.ADMIN_PANEL) },
                onSignedOut = ::goToLogin
            )
        }

        composable(Routes.ADMIN_PANEL) {
            AdminPanelScreen(
                onBack = { navController.popBackStack() },
                onCreateAccount = { navController.navigate(Routes.CREATE_ACCOUNT) }
            )
        }

        composable(Routes.CREATE_ACCOUNT) {
            CreateAccountScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.MESSAGES) {
            MessagesScreen(
                onBack = { navController.popBackStack() },
                onOpenReport = { navController.navigate(Routes.reportDetail(it)) }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onSignedOut = ::goToLogin
            )
        }

        composable(
            route = Routes.REPORT_DETAIL,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) {
            ReportDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
