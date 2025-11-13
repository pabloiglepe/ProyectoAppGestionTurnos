package com.aplimoviles.proyectoapp.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aplimoviles.proyectoapp.navigation.Destinations
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun HomeScreen(navController: NavHostController) {
    // Obtenemos el usuario actual de Firebase Auth
    val currentUser = Firebase.auth.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("Página Principal", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))

        if (currentUser != null) {

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("¡Bienvenido!", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Email:", style = MaterialTheme.typography.labelMedium)
                    Text(currentUser.email ?: "N/A", style = MaterialTheme.typography.bodyLarge)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("UID:", style = MaterialTheme.typography.labelMedium)
                    Text(currentUser.uid, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Botón para cerrar sesión
            Button(
                onClick = {
                    Firebase.auth.signOut() // Cerrar sesión
                    // Navegar a Login y limpiar la pila de navegación
                    navController.navigate(Destinations.LOGIN_ROUTE) {
                        popUpTo(Destinations.HOME_ROUTE) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp)
            ) {
                Text("Cerrar Sesión")
            }
        } else {
            // Mensaje si el usuario no está logueado (solo se vería si falla la navegación)
            Text("Error: Sesión no encontrada.", color = MaterialTheme.colorScheme.error)
        }
    }
}
