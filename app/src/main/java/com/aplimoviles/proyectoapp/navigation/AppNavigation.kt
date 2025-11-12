package com.aplimoviles.proyectoapp.navigation

import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

// Define las rutas de navegación
object Destinations {
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val HOME_ROUTE = "home"
}

fun AppNavigation(auth: FirebaseAuth) {
    val navController = rememberNavController()

    // Decide el destino inicial basado en el estado de autenticación
    val startDestination = if (auth.currentUser != null) {
        Destinations.HOME_ROUTE
    } else {
        Destinations.LOGIN_ROUTE
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Destinations.LOGIN_ROUTE) {
            // Se asume que LoginScreen ahora está en el paquete 'screens'
            LoginScreen(navController = navController)
        }
        composable(Destinations.REGISTER_ROUTE) {
            RegisterScreen(navController = navController)
        }
        composable(Destinations.HOME_ROUTE) {
            HomeScreen(navController = navController)
        }
    }
}