package com.aplimoviles.proyectoapp.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.aplimoviles.proyectoapp.models.Usuario // Importar la clase Usuario
import com.aplimoviles.proyectoapp.navigation.Destinations
import com.aplimoviles.proyectoapp.ui.theme.ProyectoAppTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import java.util.*
import android.util.Patterns // Necesario para la validación de formato de email
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.text.input.KeyboardType
import java.text.SimpleDateFormat

fun parseDateOfBirth(dateString: String): Date? {
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    format.isLenient = false // Evitar fechas inválidas
    return try {
        format.parse(dateString)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun RegisterScreen(
    navController: NavHostController,
) {
    // Estados para la entrada de datos
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }

    // Estados de la UI
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val auth = Firebase.auth

    // Obtenemos la instancia de Realtime Database (inicializada en MyApp)
    val database = Firebase.database

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
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
                    "Crear Cuenta",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Campo Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Completo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Campo Teléfono
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Campo Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                // Campo Fecha Nacimiento
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = {
                        if (it.length <= 10) {
                            dateOfBirth = it
                        }
                    },
                    label = { Text("Fecha de Nacimiento (DD/MM/AAAA)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Seleccionar Fecha",
                        )
                    },
                    maxLines = 1,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Campo Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña (min 6 caracteres)") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Botón de Registrarse
                Button(
                    onClick = {
                        errorMessage = ""
                        isLoading = true
                        scope.launch {

                            // --- LIMPIEZA DE ESPACIOS ---
                            val trimmedEmail = email.trim()
                            val trimmedPassword = password.trim()
                            val trimmedName = name.trim()
                            val trimmedPhone = phone.trim()
                            val trimmedDateOfBirth = dateOfBirth.trim()

                            // --- VALIDACIÓN BÁSICA ---
                            if (trimmedEmail.isBlank() || trimmedPassword.length < 6 || trimmedName.isBlank() || trimmedPhone.isBlank()) {
                                errorMessage = "Faltan campos o la contraseña es demasiado corta."
                                isLoading = false
                                return@launch
                            }

                            // --- VALIDACIÓN DE FORMATO DE EMAIL ---
                            if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                                errorMessage = "El formato del email es incorrecto."
                                isLoading = false
                                return@launch
                            }

                            // Parseo de fecha
                            val parsedDate = parseDateOfBirth(trimmedDateOfBirth)
                            if (parsedDate == null) { // <--- El error salta aquí
                                errorMessage = "El formato de la fecha debe ser DD/MM/AAAA válido." // Pero tu error es este
                                isLoading = false
                                return@launch
                            }

                            // 1. REGISTRO en AUTH (usando los valores trimmed)
                            auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userUid = auth.currentUser?.uid
                                        if (userUid != null) {
                                            // 2. CREAR objeto Usuario para BD
                                            val newUser = Usuario(
                                                nombre = trimmedName,
                                                email = trimmedEmail,
                                                telefono = trimmedPhone,
                                                fechaNacimiento = parsedDate,
                                                rol = "usuario"
                                            )

                                            // 3. GUARDAR en Realtime Database
                                            database.getReference("users")
                                                .child(userUid) // UID se usa como clave del nodo
                                                .setValue(newUser) // <-- GUARDA EL USUARIO
                                                .addOnCompleteListener { dbTask ->
                                                    isLoading = false
                                                    if (dbTask.isSuccessful) {
                                                        // Éxito: Navegar a Home
                                                        navController.navigate(Destinations.ELEMENT_LIST) {
                                                            popUpTo(Destinations.REGISTER_ROUTE) {
                                                                inclusive = true
                                                            }
                                                        }
                                                    } else {
                                                        // Fallo en BD: Muestra el error de la tarea de BD
                                                        errorMessage =
                                                            "Registro Auth OK, pero BD falló: ${dbTask.exception?.localizedMessage ?: "Error desconocido en BD."}"
                                                    }
                                                }
                                        }
                                    } else {
                                        // Fallo en AUTH
                                        isLoading = false
                                        errorMessage =
                                            "Fallo: ${task.exception?.localizedMessage ?: "Error de registro."}"
                                    }
                                }
                        }
                    },
                    enabled = !isLoading && email.isNotBlank() && password.length >= 6 && name.isNotBlank() && phone.isNotBlank(),
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
                        Text("Registrarse")
                    }
                }
            } // Fin de Card
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }

        // Enlace para volver a Iniciar Sesión
        TextButton(onClick = {
            navController.navigate(Destinations.LOGIN_ROUTE) {
                popUpTo(Destinations.REGISTER_ROUTE) { inclusive = true }
            }
        }) {
            Text("Ya tengo una cuenta")
        }
    }
}



