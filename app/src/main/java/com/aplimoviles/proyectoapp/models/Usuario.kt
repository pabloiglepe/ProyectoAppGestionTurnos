package com.aplimoviles.proyectoapp.models

import java.util.Date

data class Usuario(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val fechaNacimiento: Date = Date(),
    var rol: String = "usuario"
)

