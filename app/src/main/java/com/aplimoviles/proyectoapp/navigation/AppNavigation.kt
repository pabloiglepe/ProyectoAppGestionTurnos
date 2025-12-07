package com.aplimoviles.proyectoapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aplimoviles.proyectoapp.auth.ElementForm
import com.aplimoviles.proyectoapp.auth.ElementList
import com.aplimoviles.proyectoapp.auth.LoginScreen
import com.aplimoviles.proyectoapp.auth.RegisterScreen
import com.google.firebase.auth.FirebaseAuth

// Define las rutas de navegación
object Destinations {
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val ELEMENT_LIST = "elementList"
    const val ELEMENT_ID_KEY = "elementId"


    private const val ELEMENT_FORM_BASE = "elementForm"
    const val ELEMENT_FORM_ROUTE = "$ELEMENT_FORM_BASE?{$ELEMENT_ID_KEY}"

    fun elementFormRoute(elementId: String? = null): String {
        return if (elementId.isNullOrBlank()) {
            // Alta: solo la ruta base sin el parámetro
            ELEMENT_FORM_BASE
        } else {
            // Edición: ruta base con el ID como parámetro
            "$ELEMENT_FORM_BASE?$ELEMENT_ID_KEY=$elementId"
        }
    }
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
            ElementList(navController = navController)
        }
        composable(
            route = Destinations.ELEMENT_FORM_ROUTE, // Usamos la ruta con el argumento opcional
            arguments = listOf(
                navArgument(Destinations.ELEMENT_ID_KEY) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val elementId = backStackEntry.arguments?.getString(Destinations.ELEMENT_ID_KEY)
            ElementForm(navController = navController, elementId = elementId)
        }
    }
}
