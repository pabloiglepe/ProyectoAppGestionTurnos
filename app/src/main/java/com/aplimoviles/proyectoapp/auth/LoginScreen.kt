package com.aplimoviles.proyectoapp.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aplimoviles.proyectoapp.navigation.Destinations
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
// --- NUEVAS IMPORTACIONES PARA DATABASE ---
import com.google.firebase.database.database
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

// ------------------------------------------

@Composable
fun LoginScreen(
    navController: NavHostController,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val auth = Firebase.auth
    // --- INSTANCIA DE DATABASE ---
    val database = Firebase.database
    // ----------------------------

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "Iniciar Sesión",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    // Campo de Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón de Iniciar Sesión
                    Button(
                        onClick = {
                            scope.launch {

                                // A. VALIDACIÓN: Campos no vacíos y Contraseña mínima
                                if (email.isBlank() || password.isBlank()) {
                                    snackbarHostState.showSnackbar(
                                        message = "El email y la contraseña no pueden estar vacíos.",
                                        withDismissAction = true
                                    )
                                    return@launch
                                }
                                if (password.length < 6) {
                                    snackbarHostState.showSnackbar(
                                        message = "La contraseña debe tener al menos 6 caracteres.",
                                        withDismissAction = true
                                    )
                                    return@launch
                                }

                                isLoading = true

                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { authTask ->
                                        if (authTask.isSuccessful) {
                                            val userUid = auth.currentUser?.uid

                                            if (userUid != null) {
                                                database.getReference("users").child(userUid)
                                                    .child("rol")
                                                    .addListenerForSingleValueEvent(object :
                                                        ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            isLoading = false
                                                            val rol = snapshot.getValue(String::class.java) ?: "usuario"

                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    message = "Inicio de sesión exitoso. Rol: $rol",
                                                                    withDismissAction = false,
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }

                                                            navController.navigate(Destinations.ELEMENT_LIST) {
                                                                popUpTo(Destinations.LOGIN_ROUTE) {
                                                                    inclusive = true
                                                                }
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {
                                                            isLoading = false
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    message = "Fallo al obtener datos del usuario. ${error.message}",
                                                                    withDismissAction = true
                                                                )
                                                            }
                                                            auth.signOut()
                                                        }
                                                    })
                                            }
                                        } else {
                                            isLoading = false
                                            scope.launch {
                                                val errorMsg = authTask.exception?.localizedMessage
                                                    ?: "Error de autenticación desconocido."
                                                snackbarHostState.showSnackbar(
                                                    message = "Error: ${errorMsg}",
                                                    withDismissAction = true
                                                )
                                            }
                                        }
                                    }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Iniciar Sesión")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                navController.navigate(Destinations.REGISTER_ROUTE)
            }) {
                Text("¿Registrarme?")
            }
        }
    }
}