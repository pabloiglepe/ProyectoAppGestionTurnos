package com.aplimoviles.proyectoapp.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aplimoviles.proyectoapp.models.Elementos
import com.aplimoviles.proyectoapp.models.ElementListViewModel
import com.aplimoviles.proyectoapp.navigation.Destinations
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

import java.text.SimpleDateFormat
import java.util.Locale

private val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElementList(
    navController: NavController,
    viewModel: ElementListViewModel = viewModel()
) {
    val elementos by viewModel.elementos.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()

    val isAdmin = (userRole == 1) // Lógica de rol: 1 es Admin
    val auth = Firebase.auth
    val isLoggedIn = auth.currentUser != null

    Scaffold(
        topBar = { TopAppBar(title = { Text("Listado de Elementos") }) },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Destinations.elementFormRoute())
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Elemento")
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {

            if (elementos.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No hay elementos registrados.", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Esto permite que el LazyColumn ocupe el espacio restante
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(elementos, key = { it.id ?: it.nombre }) { elemento ->
                        // Aseguramos que el ID no sea nulo para las operaciones de eliminación/edición
                        elemento.id?.let { elementId ->
                            ElementListItem(
                                elemento = elemento,
                                elementId = elementId,
                                isAdmin = isAdmin,
                                navController = navController,
                                onDeleteClick = viewModel::startDelete
                            )
                        }
                    }
                }
            }

            // --- BOTÓN DE DESLOGEO AÑADIDO ABAJO ---
            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(16.dp))
            // --- FIN BOTÓN DE DESLOGEO ---
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissDeleteDialog,
                title = { Text("Confirmación de Eliminación") },
                text = { Text("¿Estás seguro de que deseas eliminar este elemento?") },
                confirmButton = { TextButton(onClick = viewModel::confirmDelete) { Text("Sí") } },
                dismissButton = { TextButton(onClick = viewModel::dismissDeleteDialog) { Text("No") } }
            )
        }


    }
}

// Composable separado para cada elemento de la lista
@Composable
fun ElementListItem(
    elemento: Elementos,
    elementId: String,
    isAdmin: Boolean,
    navController: NavController,
    onDeleteClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()


            .clickable(enabled = isAdmin) {
                navController.navigate(Destinations.elementFormRoute(elementId))
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Nombre: ${elemento.nombre}", style = MaterialTheme.typography.titleMedium)
            Text("Descripción: ${elemento.descripcion}")
            Text("Creación: ${DATE_FORMAT.format(elemento.fechaCreacion)}")

            if (isAdmin) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Botón Editar
                    TextButton(onClick = {
                        navController.navigate(Destinations.elementFormRoute(elementId))
                    }) { Text("Editar") }

                    // Botón Eliminar
                    TextButton(onClick = {
                        onDeleteClick(elementId) // Llama al VM para iniciar el diálogo y eliminación
                    }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}