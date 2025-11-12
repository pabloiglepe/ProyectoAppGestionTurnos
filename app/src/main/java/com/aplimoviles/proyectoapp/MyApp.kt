package com.aplimoviles.proyectoapp

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore


class MyApp: Application() {
    val db = Firebase.firestore
    // You can also define your Realtime Database instance here if needed
    val database = Firebase.database // Get instance via Firebase object

    override fun onCreate() {
        super.onCreate()
        // The recommended way to enable persistence for Realtime Database
        database.setPersistenceEnabled(true)
    }
}


