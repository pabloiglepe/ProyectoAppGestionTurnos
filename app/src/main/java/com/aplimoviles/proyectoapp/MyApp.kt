package com.aplimoviles.proyectoapp

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class MyApp: Application() {

    // Declaramos las instancias usando lateinit
    lateinit var db: FirebaseFirestore
    lateinit var database: FirebaseDatabase

    // URL de tu base de datos de Europa, copiado DEL LOGCAT.
    private val RTDB_URL = "https://proyectogestionturnos-default-rtdb.europe-west1.firebasedatabase.app"

    override fun onCreate() {
        super.onCreate()

        // --- 1. Inicialización explícita de Firebase (Seguridad) ---
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        // --- 2. Inicialización de las instancias (Después de la inicialización de FirebaseApp) ---
        db = Firebase.firestore

        // ¡CORRECCIÓN CLAVE! Pasar el URL al método database()
        database = Firebase.database(RTDB_URL)

        // 3. Configuración de persistencia
        database.setPersistenceEnabled(true)
    }
}