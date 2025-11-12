package com.aplimoviles.proyectoapp

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore


class MyApp: Application() {
    val db = Firebase.firestore
    val database = Firebase.database

    override fun onCreate() {
        super.onCreate()
        database.setPersistenceEnabled(true)
    }
}


