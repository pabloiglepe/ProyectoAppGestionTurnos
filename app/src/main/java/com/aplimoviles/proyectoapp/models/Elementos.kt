package com.aplimoviles.proyectoapp.models

import java.util.Date

data class Elementos (
    var nombre: String = "",
    var descripcion: String = "",
    var fechCreacion: Date = Date()
)