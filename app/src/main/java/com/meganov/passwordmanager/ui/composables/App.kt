package com.meganov.passwordmanager.ui.composables

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.meganov.passwordmanager.PasswordManagerVM

/**
 * Base app
 */
@RequiresApi(Build.VERSION_CODES.P)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App(fragmentActivity: FragmentActivity, viewModel: PasswordManagerVM) {
    val navController = rememberNavController()
    val sites by viewModel.sites.observeAsState()
    var masterPassword = viewModel.getMasterPassword()
    // Define startdestination based on previous log ins
    val startDestination = if (masterPassword == null) "setup_screen" else "masterpassword_screen"
    // Navigation graph
    NavHost(navController = navController, startDestination = startDestination) {
        composable("setup_screen") {
            BackHandler {}
            SetupScreen(
                enter = viewModel::enter,
                saveMasterPassword = viewModel::saveMasterPassword,
                fragmentActivity = fragmentActivity,
                authenticateWithFingerprint = viewModel::authenticateWithFingerprint,
                navController = navController
            )
        }
        composable("masterpassword_screen") {
            BackHandler {}
            masterPassword = viewModel.getMasterPassword()
            if (masterPassword != null) {
                MasterPasswordScreen(
                    enter = viewModel::enter,
                    decrypt = viewModel::aesDecrypt,
                    authenticateWithFingerprint = viewModel::authenticateWithFingerprint,
                    fragmentActivity = fragmentActivity,
                    hasFingerprint = viewModel.hasFingerprint,
                    realPassword = masterPassword ?: "",
                    navController = navController
                )
            }
        }
        composable("change_master") {
            BackHandler {
                navController.navigate("password_list")
            }
            ChangeMaster(
                saveMasterPassword = viewModel::saveMasterPassword,
                authenticateWithFingerprint = viewModel::authenticateWithFingerprint,
                fragmentActivity = fragmentActivity,
                hasFingerprint = viewModel.hasFingerprint,
                navController = navController
            )
        }
        composable("password_list") {
            BackHandler {}
            masterPassword = viewModel.getMasterPassword()
            PasswordList(
                sites = sites,
                fragmentActivity = fragmentActivity,
                decrypt = viewModel::aesDecrypt,
                realPassword = masterPassword ?: "",
                authenticateWithFingerprint = viewModel::authenticateWithFingerprint,
                navController = navController,
            )
        }
        composable("new_site") {
            BackHandler {
                navController.navigate("password_list")
            }
            viewModel.emptyInfo()
            NewSite(
                siteIcon = viewModel.siteIcon,
                loadIcon = viewModel::loadIcon,
                saveSite = viewModel::saveSite,
                navController = navController
            )
        }
        composable("edit_site/{siteId}") { backStackEntry ->
            BackHandler {
                navController.navigate("password_list")
            }
            val siteId = backStackEntry.arguments?.getString("siteId")?.toIntOrNull()
            val site = sites?.find { it.id == siteId }
            if (site != null) {
                viewModel.emptyInfo()
                viewModel.loadIconFromDB(site)
                EditSite(
                    site = site,
                    siteIcon = viewModel.siteIcon,
                    loadIcon = viewModel::loadIcon,
                    loadIconFromDB = viewModel::loadIconFromDB,
                    saveSite = viewModel::updateSite,
                    removeSite = viewModel::removeSite,
                    decrypt = viewModel::aesDecrypt,
                    navController = navController
                )
            }
        }
    }
}
