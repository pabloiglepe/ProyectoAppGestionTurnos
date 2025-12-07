package com.aplimoviles.proyectoapp.models

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.aplimoviles.proyectoapp.navigation.Destinations
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

class ElementFormViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val elementosRef: DatabaseReference = database.child("elementos")
    private val usersRef: DatabaseReference = database.child("users")

    private val elementId: String? = savedStateHandle[Destinations.ELEMENT_ID_KEY]

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre

    private val _descripcion = MutableStateFlow("")
    val descripcion: StateFlow<String> = _descripcion

    private val _saveStatus = MutableStateFlow<String?>(null)
    val saveStatus: StateFlow<String?> = _saveStatus

    private val _userRole = MutableStateFlow(2)
    val userRole: StateFlow<Int> = _userRole

    private val _isRoleLoaded = MutableStateFlow(false)
    val isRoleLoaded: StateFlow<Boolean> = _isRoleLoaded

    val isEditing: Boolean = elementId != null

    init {
        cargarRolUsuario()
        if (isEditing) {
            cargarElementoParaEdicion()
        }
    }

    fun updateNombre(newNombre: String) {
        _nombre.value = newNombre
    }

    fun updateDescripcion(newDescripcion: String) {
        _descripcion.value = newDescripcion
    }

    private fun cargarRolUsuario() {
        val uid = auth.currentUser?.uid ?: return
        usersRef.child(uid).get()
            .addOnSuccessListener { snap ->
                val rolString = snap.getValue(Usuario::class.java)?.rol
                _userRole.value = when (rolString) {
                    "admin" -> 1
                    else -> 2
                }
                _isRoleLoaded.value = true
            }
            .addOnFailureListener { e ->
                Log.e("ElementFormVM", "Error leyendo rol: ${e.message}", e)
                _userRole.value = 2
            }
    }

    private fun cargarElementoParaEdicion() {
        elementId?.let { id ->
            elementosRef.child(id).get()
                .addOnSuccessListener { snapshot ->
                    val elemento = snapshot.getValue(Elementos::class.java)
                    if (elemento != null) {
                        // Rellenar los campos del formulario
                        _nombre.value = elemento.nombre
                        _descripcion.value = elemento.descripcion
                    } else {
                        _saveStatus.value = "Error: Elemento no encontrado para editar."
                    }
                }
                .addOnFailureListener { e ->
                    _saveStatus.value = "Error al cargar datos: ${e.message}"
                }
        }
    }

    fun guardarElemento() {
        if (_userRole.value != 1) {
            _saveStatus.value = "ERROR: Solo los administradores pueden crear/modificar elementos."
            return
        }

        val nombreActual = _nombre.value.trim()
        val descripcionActual = _descripcion.value.trim()

        if (nombreActual.isEmpty() || descripcionActual.isEmpty()) {
            _saveStatus.value = "ERROR: Nombre y descripción no pueden estar vacíos."
            return
        }

        val elementoAGuardar = Elementos(
            nombre = nombreActual,
            descripcion = descripcionActual,
            fechaCreacion = Date(),
            id = elementId
        )

        val operacionFirebase = if (isEditing && elementId != null) {
            // Editar: elementosRef.child(elementId).setValue(elemento)
            Log.d("ElementFormVM", "Ejecutando EDICIÓN para ID: $elementId")
            elementosRef.child(elementId).setValue(elementoAGuardar)
        } else {
            // Crear: elementosRef.push().setValue(elemento)
            Log.d("ElementFormVM", "Ejecutando CREACIÓN")
            elementosRef.push().setValue(elementoAGuardar)
        }

        operacionFirebase
            .addOnSuccessListener {
                _saveStatus.value = "Éxito: Elemento guardado correctamente."
            }
            .addOnFailureListener { e ->
                // Esto podría incluir "PERMISSION_DENIED" de las reglas de seguridad
                if (e.message?.contains("permission_denied") == true) {
                    _saveStatus.value =
                        "Error de Permiso: No tienes acceso para escribir en la base de datos."
                } else {
                    _saveStatus.value = "Error al guardar en DB: ${e.message}"
                }
            }
    }
}