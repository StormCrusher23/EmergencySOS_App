package com.storm.sosremasterd.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.storm.sosremasterd.screens.*
import com.storm.sosremasterd.viewmodel.TriggerViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.storm.sosremasterd.utils.PreferencesManager
import android.app.Activity
import androidx.activity.compose.BackHandler

@Composable
fun NavigationGraph(navController: NavHostController) {
    val context = LocalContext.current
    val triggerViewModel: TriggerViewModel = viewModel()
    val preferencesManager = remember { PreferencesManager(context) }

    // Determine start destination based on setup completion
    val startDestination = remember(preferencesManager.hasCompletedSetup) {
        if (preferencesManager.hasCompletedSetup) {
            Screen.SentryMode.route
        } else {
            Screen.Welcome.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onGetStarted = {
                    navController.navigate(Screen.HowToUse.route)
                }
            )
        }
        composable(Screen.HowToUse.route) {
            HowToUseScreen(
                onContinue = {
                    navController.navigate(Screen.Legal.route)
                }
            )
        }
        composable(Screen.Legal.route) {
            LegalScreen(
                onAgree = {
                    navController.navigate(Screen.Setup.route)
                }
            )
        }
        composable(Screen.Setup.route) {
            SetupScreen(
                triggerViewModel = triggerViewModel,
                preferencesManager = preferencesManager,
                onFinish = {
                    preferencesManager.hasCompletedSetup = true
                    navController.navigate(Screen.SentryMode.route) {
                        // Clear the entire back stack
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.SentryMode.route) { 
            BackHandler {
                // Close the app when back is pressed on SentryMode screen
                (context as? Activity)?.finish()
            }
            
            SentryModeScreen(
                context = context,
                onSetupClick = {
                    navController.navigate(Screen.Setup.route)
                },
                onContactSetupClick = {
                    navController.navigate(Screen.ContactSetup.route)
                }
            )
        }
        composable(Screen.ContactSetup.route) {
            ContactSetupScreen(
                preferencesManager = preferencesManager,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
} 