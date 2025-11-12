package com.aplimoviles.proyectoapp.models

import java.util.Date

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val contraseña: String = "",
    val telefono: String = "",
    val fechaNacimiento: Date = Date(),
)

