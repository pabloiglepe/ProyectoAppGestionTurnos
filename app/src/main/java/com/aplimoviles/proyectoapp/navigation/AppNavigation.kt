package com.aplimoviles.proyectoapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aplimoviles.proyectoapp.auth.ElementForm
import com.aplimoviles.proyectoapp.auth.HomeScreen
import com.aplimoviles.proyectoapp.auth.LoginScreen
import com.aplimoviles.proyectoapp.auth.RegisterScreen
import com.google.firebase.auth.FirebaseAuth

// Define las rutas de navegación
object Destinations {
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val ELEMENT_LIST = "elementList"
    const val ELEMENT_FORM = "elementForm"
}

@Composable
fun AppNavigation(auth: FirebaseAuth) {
    val navController = rememberNavController()

    // Decide el destino inicial basado en el estado de autenticación
    val startDestination = if (auth.currentUser != null) {
        Destinations.ELEMENT_LIST
    } else {
        Destinations.LOGIN_ROUTE
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Destinations.LOGIN_ROUTE) {
            LoginScreen(navController = navController)
        }
        composable(Destinations.REGISTER_ROUTE) {
            RegisterScreen(navController = navController)
        }
        composable(Destinations.ELEMENT_LIST) {
            HomeScreen(navController = navController)
        }
        composable(Destinations.ELEMENT_FORM) {
            ElementForm(navController = navController)
        }
    }
}
