package com.aplimoviles.proyectoapp.models

import java.util.Date

data class Usuario(
    val nombre: String? = null,
    val email: String? = null,
    val contraseña: String? = null,
    val telefono: String? = null,
    val fechaNacimiento: Date? = null,
)

