package com.aplimoviles.proyectoapp.models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class ElementListViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    private val _userRole = MutableStateFlow(2) // 1: Admin, 2: Usuario
    val userRole: StateFlow<Int> = _userRole

    private val _elementos = MutableStateFlow<List<Elementos>>(emptyList())
    val elementos: StateFlow<List<Elementos>> = _elementos

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog

    private var selectedElementId: String? = null

    // Listener de Firebase (para asegurar la limpieza)
    private val elementosListener: ValueEventListener

    init {
        elementosListener = createElementosListener()
        cargarRolUsuario()
        database.child("elementos").addValueEventListener(elementosListener)
    }

    private fun createElementosListener(): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { snap ->
                    try {
                        val elemento = snap.getValue(Elementos::class.java)
                        // Asignar la clave de Firebase al campo 'id'
                        elemento?.id = snap.key
                        elemento
                    } catch (e: Exception) {
                        Log.e("ElementListVM", "Error convirtiendo Elemento: ${snap.key}", e)
                        null
                    }
                }
                _elementos.value = lista
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ElementListVM", "Error leyendo elementos: ${error.message}")
            }
        }
    }

    private fun cargarRolUsuario() {
        val uid = auth.currentUser?.uid ?: return

        database.child("users").child(uid).get()
            .addOnSuccessListener { snap ->
                val usuario = snap.getValue(Usuario::class.java)

                val rolString = usuario?.rol

                val rolInt = when (rolString) {
                    "admin" -> 1
                    "usuario" -> 2
                    else -> 2
                }

                // 5. Asignamos el valor Int al StateFlow
                _userRole.value = rolInt

            }
            .addOnFailureListener { e ->
                Log.e("ElementListVM", "Error leyendo rol: ${e.message}", e)
                _userRole.value = 2
            }
    }

    fun startDelete(elementId: String) {
        selectedElementId = elementId
        _showDeleteDialog.value = true
    }

    fun confirmDelete() {
        val idToDelete = selectedElementId
        if (idToDelete != null) {
            database.child("elementos").child(idToDelete).removeValue()
                .addOnSuccessListener { Log.d("ElementListVM", "Elemento $idToDelete eliminado.") }
                .addOnFailureListener { e -> Log.e("ElementListVM", "Fallo al eliminar: ${e.message}") }
        }
        selectedElementId = null
        _showDeleteDialog.value = false
    }

    fun dismissDeleteDialog() {
        selectedElementId = null
        _showDeleteDialog.value = false
    }

    override fun onCleared() {
        super.onCleared()
        database.child("elementos").removeEventListener(elementosListener)
    }
}