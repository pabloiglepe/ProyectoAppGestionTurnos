package com.aplimoviles.proyectoapp.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults // <-- Mantener import
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aplimoviles.proyectoapp.models.ElementFormViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


// Define tus rutas de navegación aquí (asumiendo que están definidas en otro archivo)
object Destinations {
    const val LOGIN_ROUTE = "login"
    const val ELEMENT_LIST = "element_list"
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElementForm(
    navController: NavController,
    elementId: String?,
    viewModel: ElementFormViewModel = viewModel()
) {
    val context = LocalContext.current
    val auth = Firebase.auth

    // Observar estados del ViewModel
    val nombre by viewModel.nombre.collectAsState()
    val descripcion by viewModel.descripcion.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    val isRoleLoaded by viewModel.isRoleLoaded.collectAsState()
    val isAdmin = (userRole == 1)
    val isLoggedIn = auth.currentUser != null

    LaunchedEffect(saveStatus) {
        saveStatus?.let { status ->
            Toast.makeText(context, status, Toast.LENGTH_LONG).show()
            if (status.startsWith("Éxito:")) {
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Editar Elemento" else "Crear Elemento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp) // Solo padding horizontal aquí
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- CONTENIDO PRINCIPAL (ADMIN O ACCESO DENEGADO) ---
            if (isAdmin) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = viewModel::updateNombre,
                        label = { Text("Nombre") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp, top = 16.dp) // Añadir padding superior
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = viewModel::updateDescripcion,
                        label = { Text("Descripción") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = viewModel::guardarElemento,
                        enabled = isAdmin && isRoleLoaded,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (viewModel.isEditing) "Guardar Cambios" else "Crear Elemento")
                    }
                }

            } else {
                if (isRoleLoaded && !isAdmin) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp), // Darle un tamaño fijo para que no desaparezca
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Acceso Denegado. Solo los administradores pueden acceder al formulario.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            // --- FIN CONTENIDO PRINCIPAL ---

            // --- SEPARADOR PARA EMPUJAR EL BOTÓN DE LOGOUT ABAJO ---
            Spacer(modifier = Modifier.weight(1f)) // Este spacer ocupa todo el espacio libre disponible.

            // --- CÓDIGO DEL BOTÓN DE DESLOGEO (FIJO ABAJO) ---
            if (isLoggedIn) {
                Button(
                    onClick = {
                        // 1. Cerrar sesión en Firebase
                        auth.signOut()

                        // 2. Navegar a Login y limpiar la pila de navegación
                        navController.navigate(Destinations.LOGIN_ROUTE) {
                            popUpTo(Destinations.ELEMENT_LIST) { inclusive = true }
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

            Spacer(modifier = Modifier.height(16.dp)) // Padding inferior para el botón
            // --- FIN BOTÓN DE DESLOGEO ---
        }
    }
}