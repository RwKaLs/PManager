package com.meganov.passwordmanager.ui.composables

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.meganov.passwordmanager.PasswordManagerVM

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App(viewModel: PasswordManagerVM) {
    val navController = rememberNavController()
    val sites by viewModel.sites.observeAsState()
    NavHost(navController = navController, startDestination = "password_list") {
        composable("setup_screen") {
            SetupScreen(navController = navController)
        }
        composable("password_list") {
            PasswordList(
                sites = sites,
                navController = navController
            )
        }
        composable("new_site") {
            viewModel.emptyInfo()
            NewSite(
                siteIcon = viewModel.siteIcon,
                loadIcon = viewModel::loadIcon,
                saveSite = viewModel::saveSite,
                navController = navController
            )
        }
        composable("edit_site/{siteId}") { backStackEntry ->
            val siteId = backStackEntry.arguments?.getString("siteId")?.toIntOrNull()
            val site = sites?.find { it.id == siteId }
            if (site != null) {
                viewModel.emptyInfo()
                viewModel.loadIconFromDB(site)
                EditSite(
                    site = site,
                    siteIcon = viewModel.siteIcon,
                    loadIcon = viewModel::loadIcon,
                    saveSite = viewModel::updateSite,
                    removeSite = viewModel::removeSite,
                    navController = navController
                )
            }
        }
    }
}

