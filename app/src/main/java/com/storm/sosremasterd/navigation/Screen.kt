package com.storm.sosremasterd.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object HowToUse : Screen("how_to_use")
    object Legal : Screen("legal")
    object Setup : Screen("setup")
    object SentryMode : Screen("sentry_mode")
    object ContactSetup : Screen("contact_setup")
} 