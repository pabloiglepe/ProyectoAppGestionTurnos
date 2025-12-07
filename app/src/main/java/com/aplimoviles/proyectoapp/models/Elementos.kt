package com.aplimoviles.proyectoapp.models

import java.util.Date

data class Elementos (
    val nombre: String = "",
    val descripcion: String = "",
    val fechaCreacion: Date = Date(),
    var id: String? = null
) {
     constructor() : this("", "", Date(), null)
 }